package me.cjcrafter.schematicbrushes.commands;

import me.cjcrafter.schematicbrushes.API;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender,@Nonnull Command cmd,@Nonnull String label,@Nonnull String[] args) {
        if (!(label.equalsIgnoreCase("sb") || label.equalsIgnoreCase("schematicbrushes")))
            return getList();

        if (args.length == 0) return getList("help", "get", "give", "list", "reload");
        else if (args[0].equals("get")) {
            if (args.length == 2) return new ArrayList<>(API.brushes.keySet());
        }
        else if (args[0].equals("give")) {
            if (args.length == 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if (args.length == 3) return new ArrayList<>(API.brushes.keySet());
        }
        return getList("help", "get", "give", "list", "reload");
    }

    private static List<String> getList(String...strings) {
        return new ArrayList<>(Arrays.asList(strings));
    }

    private static boolean in(int lower, int i, int upper) {
        return i > lower && i < upper;
    }
}
