package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.NetManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents implements Listener{

    private Server server;
    private final Voicechat main;

    @EventHandler
    public void loggedIn(PlayerJoinEvent e) {
        initializePlayerConnection(e.getPlayer());
    }
    @EventHandler
    public void loggedOut(PlayerQuitEvent e) {
        playerLoggedOut(e.getPlayer());
    }

    public ServerVoiceEvents(Voicechat main) {
        this.main = main;
        serverStarting();
        InitPacket.SECRET.registerOutgoingChannel(main);
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public void serverStarting() {
        if (server != null) {
            server.close();
            server = null;
        }
        try {
            server = new Server(main, Voicechat.SERVER_CONFIG.voiceChatPort.get());
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
        NetManager.sendToClient(main, player, new InitPacket(secret, Voicechat.SERVER_CONFIG.voiceChatPort.get(), (ServerConfig.Codec) Voicechat.SERVER_CONFIG.voiceChatCodec.get(), Voicechat.SERVER_CONFIG.voiceChatMtuSize.get(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get(), Voicechat.SERVER_CONFIG.keepAlive.get(), Voicechat.SERVER_CONFIG.groupsEnabled.get()));
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
