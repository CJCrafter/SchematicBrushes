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

        List<String> returnValue = null;

        if (args[0].equals("get")) {
            if (args.length == 2) returnValue = new ArrayList<>(API.brushes.keySet());
        }
        else if (args[0].equals("give")) {
            if (args.length == 2) returnValue = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if (args.length == 3) returnValue =  new ArrayList<>(API.brushes.keySet());
        }
        else if (!getList("help", "get", "give", "list", "reload").contains(args[0])) returnValue = getList("help", "get", "give", "list", "reload");

        if (returnValue == null) return null;
        return returnValue.stream().filter(str -> str.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

    private static List<String> getList(String...strings) {
        return Arrays.asList(strings);
    }
}
