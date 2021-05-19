package de.maxhenkel.voicechat.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ForgeHandshakeInterceptListener implements PacketListener {
    private final Voicechat main;
    public ForgeHandshakeInterceptListener(Voicechat main) {
        this.main = main;
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        if(packetEvent.getPacketType() != PacketType.Login.Server.SUCCESS) {
            return;
        }
        Player ply = packetEvent.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeByte(0);
            buf.writeByte(2);
            buf.writeInt(0);
            PacketContainer packet = Voicechat.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            packet.getMinecraftKeys().write(0, new MinecraftKey("minecraft","FML|HS"));
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
            packetDataSerializer.writeInt(buf.readableBytes());
            packetDataSerializer.writeBytes(buf);
            packet.getModifier().withType(PacketDataSerializer.class).write(0, packetDataSerializer);
            try {
                Voicechat.PROTOCOL_MANAGER.sendServerPacket(ply, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1);
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        if(packetEvent.getPacketType() != PacketType.Play.Client.CUSTOM_PAYLOAD || !"minecraft:FML|HS".equals(packetEvent.getPacket().getMinecraftKeys().read(0).getFullKey())) {
            return;
        }
        
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return null;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return null;
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }
}
