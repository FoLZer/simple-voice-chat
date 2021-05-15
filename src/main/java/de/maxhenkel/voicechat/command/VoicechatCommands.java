package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SetGroupPacket;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VoicechatCommands implements CommandExecutor {
    Voicechat main;
    public VoicechatCommands(Voicechat main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player ply = (Player) sender;
        if(args.length < 1) {
            return true;
        }
        if("test".equals(args[0])) {
            if(sender.hasPermission("voicechat.test")) {
                if(args.length >= 2) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    Server server = Voicechat.SERVER.getServer();
                    if(server == null) {
                        ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.voice_chat_unavailable").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                        return true;
                    }
                    ClientConnection clientConnection = server.getConnections().get(target.getUniqueId());
                    if(clientConnection == null) {
                        ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.client_not_connected").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                        return true;
                    }
                    try {
                        ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.sending_packet")));
                        long timestamp = System.currentTimeMillis();
                        server.getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                            @Override
                            public void onPong(PingPacket packet) {
                                ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.packet_received").args(Component.text(System.currentTimeMillis() - timestamp))));
                            }

                            @Override
                            public void onTimeout() {
                                ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.packet_timed_out").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                            }
                        });
                        ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.packet_sent_waiting")));
                    } catch(Exception e) {
                        ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.failed_to_send_packet").args( Component.text(e.getMessage())).style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                        e.printStackTrace();
                        return true;
                    }
                }
            }
        } else if("invite".equals(args[0])) {
            if(args.length >= 2) {
                Server server = Voicechat.SERVER.getServer();
                if (server == null) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.voice_chat_unavailable").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }
                PlayerState state = server.getPlayerStateManager().getState(ply.getUniqueId());
                if (state == null || !state.hasGroup()) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.not_in_group").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                target.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.invite").args(
                        ply.displayName(),
                        Component.text(state.getGroup()).style(Style.style(TextColor.color(HSVLike.fromRGB(128,128,128)))),
                        Component.text('[').append(Component.translatable("message.voicechat.accept_invite").style(Style.style()
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + state.getGroup()))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.voicechat.accept_invite.hover")))))
                                .append(Component.text(']')).style(Style.style(TextColor.color(0,255,0))))
                ));
                ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.invite_successful").args(target.displayName())));
                return true;
            }
        } else if("join".equals(args[0])) {
            if(args.length >= 2) {
                if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.groups_disabled").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }
                Server server = Voicechat.SERVER.getServer();
                if (server == null) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.voice_chat_unavailable").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }
                String groupName = args[1];
                if (groupName.length() > 16) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.group_name_too_long").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }

                if (!Voicechat.GROUP_REGEX.matcher(groupName).matches()) {
                    ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.invalid_group_name").style(Style.style(TextColor.color(HSVLike.fromRGB(255,0,0))))));
                    return true;
                }

                NetManager.sendToClient(main, ply, new SetGroupPacket(groupName));
                ply.sendMessage(Component.text("[Voicechat] ").append(Component.translatable("message.voicechat.join_successful")
                .args(Component.text(groupName).style(Style.style(TextColor.color(HSVLike.fromRGB(128,128,128)))))
                ));
                return true;
            }
        }
        return true;
    }
}
