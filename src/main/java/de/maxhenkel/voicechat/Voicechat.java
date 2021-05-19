package de.maxhenkel.voicechat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.maxhenkel.voicechat.api.ResourceLocation;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.command.VoicechatCommandsAutocompleter;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.events.ForgeHandshakeInterceptListener;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

public class Voicechat extends JavaPlugin {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    @Nullable
    public static ServerConfig SERVER_CONFIG;
    public static final ResourceLocation FABRIC_INIT = new ResourceLocation(MODID, "init");
    public static final ResourceLocation FORGE_DEFAULT = new ResourceLocation(MODID, "default");

    public static int COMPATIBILITY_VERSION = -1;
    public static final Pattern GROUP_REGEX = Pattern.compile("^[a-zA-Z0-9-_]{1,16}$");

    public static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
    private final List<UUID> forgePlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        loadConfig();
        registerChannels();
        registerForgeTest();

        //TODO: Needs fixing
        //getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        SERVER = new ServerVoiceEvents(this);

        getCommand("voicechat").setExecutor(new VoicechatCommands(this));
        getCommand("voicechat").setTabCompleter(new VoicechatCommandsAutocompleter());
    }

    private void loadConfig() {
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
    }
    private void registerChannels() {
        FABRIC_INIT.registerIncomingChannel(this, (channel, player, message) -> {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            int clientCompatibilityVersion = in.readInt();
            LOGGER.info("Got INIT answer : {}", clientCompatibilityVersion);

            if (clientCompatibilityVersion != COMPATIBILITY_VERSION) {
                LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", player.getAddress().getAddress().getHostAddress(), COMPATIBILITY_VERSION, clientCompatibilityVersion);
                player.kick(Component.translatable("message.voicechat.incompatible_version"));
            }
        });
        FABRIC_INIT.registerOutgoingChannel(this);
        FORGE_DEFAULT.registerIncomingChannel(this, (channel, player, message) -> {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            int clientCompatibilityVersion = in.readInt();
            LOGGER.info("Got INIT answer : {}", clientCompatibilityVersion);

            if (clientCompatibilityVersion != COMPATIBILITY_VERSION) {
                LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", player.getAddress().getAddress().getHostAddress(), COMPATIBILITY_VERSION, clientCompatibilityVersion);
                player.kick(Component.translatable("message.voicechat.incompatible_version"));
            }
        });
        FORGE_DEFAULT.registerOutgoingChannel(this);
    }
    private void registerForgeTest() {
        ResourceLocation FMLHS = new ResourceLocation("minecraft", "FML|HS");
        FMLHS.registerOutgoingChannel(this);
        FMLHS.registerIncomingChannel(this, (channel, player, message) -> {
            forgePlayers.add(player.getUniqueId());
        });
        PROTOCOL_MANAGER.addPacketListener(new ForgeHandshakeInterceptListener(this));
    }
    public boolean isForgePlayer(UUID uuid) {
        return forgePlayers.contains(uuid);
    }
    public static void sendPacket(Voicechat main, Player ply, PacketContainer packet) throws InvocationTargetException {
        if(main.isForgePlayer(ply.getUniqueId())) {
            packet.getMinecraftKeys().write(0, new MinecraftKey("voicechat","default"));
        }
        PROTOCOL_MANAGER.sendServerPacket(ply, packet);
    }
}
