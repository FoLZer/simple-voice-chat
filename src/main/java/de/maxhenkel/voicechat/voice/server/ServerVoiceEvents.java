package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.NetManager;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents {

    private Server server;

    public ServerVoiceEvents() {
        serverStarting();
        PlayerEvents.PLAYER_LOGGED_IN.register(this::initializePlayerConnection);
        PlayerEvents.PLAYER_LOGGED_OUT.register(this::playerLoggedOut);
    }

    public void serverStarting() {
        if (server != null) {
            server.close();
            server = null;
        }
        try {
            server = new Server(Voicechat.SERVER_CONFIG.voiceChatPort.get());
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializePlayerConnection(Player player) {
        if (server == null) {
            return;
        }

        UUID secret = server.getSecret(player.getUniqueId());
        NetManager.sendToClient(player, new InitPacket(secret, Voicechat.SERVER_CONFIG.voiceChatPort.get(), (ServerConfig.Codec) Voicechat.SERVER_CONFIG.voiceChatCodec.get(), Voicechat.SERVER_CONFIG.voiceChatMtuSize.get(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get(), Voicechat.SERVER_CONFIG.keepAlive.get(), Voicechat.SERVER_CONFIG.groupsEnabled.get()));
        Voicechat.LOGGER.info("Sent secret to " + player.displayName());
    }

    public void playerLoggedOut(Player player) {
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUniqueId());
        Voicechat.LOGGER.info("Disconnecting client " + player.displayName());
    }

    @Nullable
    public Server getServer() {
        return server;
    }
}
