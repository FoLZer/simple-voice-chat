package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

public interface Packet<T extends Packet> {

    ResourceLocation getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

}
