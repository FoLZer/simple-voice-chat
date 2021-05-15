package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class NetManager {
    static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static <T extends Packet<T>> void registerServerReceiver(Voicechat main, Class<T> packetType, ServerReceiver<T> packetReceiver) {
        try {
            T dummyPacket = packetType.newInstance();
            dummyPacket.getID().registerOutgoingChannel(main);
            dummyPacket.getID().registerIncomingChannel(main, new PluginMessageListener() {
                @Override
                public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {
                    try {
                        T packet = packetType.newInstance();
                        packet.fromBytes(new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
                        packetReceiver.onPacket(player, s, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void sendToClient(Voicechat main, Player player, Packet<?> packetA) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            packetA.toBytes(buf);
            packet.getMinecraftKeys().write(0, new MinecraftKey("voicechat",packetA.getID().toString().split(":")[1]));
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
            //packetDataSerializer.writeInt(buf.readableBytes());
            packetDataSerializer.writeBytes(buf);
            packet.getModifier().withType(PacketDataSerializer.class).write(0, packetDataSerializer);
            protocolManager.sendServerPacket(player, packet);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static interface ServerReceiver<T extends Packet<T>> {
        void onPacket(Player player, String channel, T packet);
    }
}
