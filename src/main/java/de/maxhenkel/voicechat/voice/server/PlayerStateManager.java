package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerStateManager implements Listener {

    private Map<UUID, PlayerState> states;

    @EventHandler
    public void loggedOut(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }
    @EventHandler
    public void loggedIn(PlayerJoinEvent e) {
        notifyPlayer(e.getPlayer());
    }

    Voicechat main;

    public PlayerStateManager(Voicechat main) {
        this.main = main;
        states = new HashMap<>();
        PlayerStatesPacket.PLAYER_STATES.registerOutgoingChannel(main);
        NetManager.registerServerReceiver(main, PlayerStatePacket.class, (player, s, packet) -> {
            PlayerState state = packet.getPlayerState();
            state.setGameProfile(((CraftPlayer)player).getProfile());
            states.put(player.getUniqueId(), state);
            broadcastState(state);
        });

        main.getServer().getPluginManager().registerEvents(this, main);
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        Bukkit.getOnlinePlayers().forEach(p -> NetManager.sendToClient(main, p, packet));
    }

    private void notifyPlayer(Player player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(main, player, packet);
        broadcastState(new PlayerState(false, true, ((CraftPlayer)player).getProfile()));
    }

    private void removePlayer(Player player) {
        states.remove(player.getUniqueId());
        broadcastState(new PlayerState(true, true, ((CraftPlayer)player).getProfile())); //TODO maybe remove
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public List<PlayerState> getStates() {
        return new ArrayList<>(states.values());
    }

}
