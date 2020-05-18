package me.cjcrafter.core.commands;

import me.cjcrafter.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MainCommand extends BukkitCommand {

    protected String permission;
    protected SubCommands commands;

    protected MainCommand(String name, String permission) {
        super(name);

        this.permission = permission;
        this.commands = new SubCommands();

        Permission perm = new Permission(this.permission);
        Bukkit.getPluginManager().addPermission(perm);
    }

    protected void registerHelp() {
        // Get the shortest alias for the help command
        String prefix = getName();
        for (String string: super.getAliases()) {
            if (string.length() < prefix.length()) {
                prefix = string;
            }
        }

        SubCommand dummy = new SubCommand(prefix, "help", "General help information for commands", "<args>") {
            @Override
            public void execute(CommandSender sender, String[] args) {}
        };

        commands.register(dummy);
    }

    public boolean help(CommandSender sender, String[] args) {
        if (args.length == 0) {
            commands.get().forEach((key, cmd) -> MessageUtils.message(sender, "&7➢  " + cmd.toString()));
            return true;
        }
        else {
            return commands.sendHelp(sender, args);
        }
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(permission)) {
            MessageUtils.message(sender, "&cInvalid permissions!");
            commands.execute("info", sender, new String[0]);
            return true;
        }

        // If this stays as false, send help message
        boolean isSuccessful = false;

        if (args.length > 0) {
            if (args[0].equals("help")) {
                isSuccessful = help(sender, Arrays.copyOfRange(args, 1, args.length));
            }
            else {
                isSuccessful = commands.execute(args[0], sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        if (!isSuccessful) {
            help(sender, args);
        }
        return true;
    }

    @Nonnull
    @Override
    public List<String> tabComplete(@Nonnull CommandSender sender, @Nonnull String alias, @Nonnull String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission(permission)) {
            return new ArrayList<>();
        }

        List<String> temp;

        // Command has arguments
        if (args.length > 1) {

            // Should the we follow help command tab completions?
            if (args[0].equals("help")) {
                if (args.length == 2) temp = new ArrayList<>(commands.keys());
                else temp = commands.tabCompletions(args[1], Arrays.copyOfRange(args, 2, args.length));

            // Let subcommands handle tab completions
            } else {
                temp = commands.tabCompletions(args[0], Arrays.copyOfRange(args, 1, args.length));
            }
        } else {
            temp = new ArrayList<>(commands.keys());
        }

        // Temp should never be null
        return temp.stream().filter(string -> string.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }
}
