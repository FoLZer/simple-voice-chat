package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class NetManager {

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

    public static void sendToClient(Voicechat main, Player player, Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        player.sendPluginMessage(main, packet.getID().toString(), buffer.array());
    }

    public static interface ServerReceiver<T extends Packet<T>> {
        void onPacket(Player player, String channel, T packet);
    }
}
