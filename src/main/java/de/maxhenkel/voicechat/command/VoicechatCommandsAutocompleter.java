package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VoicechatCommandsAutocompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args[0].equals("test")) {
            if(commandSender.hasPermission("voicechat.test")) {
                return Bukkit.getOnlinePlayers().stream().map(player -> { return player.getName(); }).collect(Collectors.toList());
            }
            return null;
        }
        if(args[0].equals("invite")) {
            return Bukkit.getOnlinePlayers().stream().map(player -> { return player.getName(); }).collect(Collectors.toList());
        }
        if(args[0].equals("join")) {
            return Collections.singletonList("group");
        }
        if(args.length < 2) {
            return Arrays.asList("test", "invite", "join");
        }
        return null;
    }
}
