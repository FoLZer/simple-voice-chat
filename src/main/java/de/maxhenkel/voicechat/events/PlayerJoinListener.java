package de.maxhenkel.voicechat.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    final Voicechat main;
    public PlayerJoinListener(Voicechat main) {
        this.main = main;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            PacketContainer packet = Voicechat.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            try {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeInt(Voicechat.COMPATIBILITY_VERSION);
                packet.getMinecraftKeys().write(0, new MinecraftKey("voicechat","init"));
                PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                packetDataSerializer.writeInt(buf.readableBytes());
                packetDataSerializer.writeBytes(buf);
                packet.getModifier().withType(PacketDataSerializer.class).write(0, packetDataSerializer);
                Voicechat.sendPacket(main, e.getPlayer(), packet);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            Voicechat.LOGGER.info("Sent INIT packet");
        }, 20);
    }
}
