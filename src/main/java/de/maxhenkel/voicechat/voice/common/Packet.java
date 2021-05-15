package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.FriendlyByteBuf;

public interface Packet<T extends Packet> {

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

}
