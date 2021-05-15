package de.maxhenkel.voicechat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.maxhenkel.voicechat.api.FriendlyByteBuf;
import de.maxhenkel.voicechat.api.ResourceLocation;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class Voicechat extends JavaPlugin {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    @Nullable
    public static ServerConfig SERVER_CONFIG;

    public static final ResourceLocation INIT = new ResourceLocation(Voicechat.MODID, "init");
    public static int COMPATIBILITY_VERSION = -1;

    public static final Pattern GROUP_REGEX = Pattern.compile("^[a-zA-Z0-9-_]{1,16}$");

    @Override
    public void onEnable() {
        try {
            LOGGER.info(getClass().getClassLoader());
            InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
            Properties props = new Properties();
            props.load(in);
            COMPATIBILITY_VERSION = Integer.parseInt(props.getProperty("compatibility_version"));
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }
        ConfigBuilder.create(getDataFolder().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));

        INIT.registerIncomingChannel(this, new PluginMessageListener() {
            @Override
            public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
                ByteArrayDataInput in = ByteStreams.newDataInput(message);
                int clientCompatibilityVersion = in.readInt();
                LOGGER.info("Got INIT answer : {}", clientCompatibilityVersion);

                if (clientCompatibilityVersion != Voicechat.COMPATIBILITY_VERSION) {
                    Voicechat.LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", player.getAddress().getAddress().getHostAddress(), Voicechat.COMPATIBILITY_VERSION, clientCompatibilityVersion);
                    player.kick(Component.translatable("message.voicechat.incompatible_version"));
                }
            }
        });
        INIT.registerOutgoingChannel(this);
        class QUERY_START implements Listener {
            final Voicechat main;
            QUERY_START(Voicechat main) {
                this.main = main;
            }
            @EventHandler
            public void onLogin(PlayerJoinEvent e) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
                    try {
                        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                        buf.writeInt(COMPATIBILITY_VERSION);
                        packet.getMinecraftKeys().write(0, new MinecraftKey("voicechat","init"));
                        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                        packetDataSerializer.writeInt(buf.readableBytes());
                        packetDataSerializer.writeBytes(buf);
                        packet.getModifier().withType(PacketDataSerializer.class).write(0, packetDataSerializer);
                        protocolManager.sendServerPacket(e.getPlayer(), packet);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    LOGGER.info("Sent INIT packet");
                }, 20);
            }
        }
        getServer().getPluginManager().registerEvents(new QUERY_START(this), this);

        SERVER = new ServerVoiceEvents(this);

        //CommandRegistrationCallback.EVENT.register(VoicechatCommands::register);
        //getCommand("voicechat").setExecutor(new VoicechatCommands());
    }
}
