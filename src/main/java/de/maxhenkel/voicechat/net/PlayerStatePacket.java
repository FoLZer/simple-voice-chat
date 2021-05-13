package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ResourceLocation;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "player_state");

    private PlayerState playerState;

    public PlayerStatePacket() {

    }

    public PlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public ResourceLocation getID() {
        return PLAYER_STATE;
    }

    @Override
    public PlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        playerState.toBytes(buf);
    }

}
