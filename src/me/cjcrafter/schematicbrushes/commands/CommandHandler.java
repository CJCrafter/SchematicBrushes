package me.cjcrafter.schematicbrushes.commands;

import me.cjcrafter.schematicbrushes.API;
import me.cjcrafter.schematicbrushes.Brush;
import me.cjcrafter.schematicbrushes.SchematicBrushes;
import me.cjcrafter.schematicbrushes.gui.Holder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class CommandHandler implements CommandExecutor {

    private SchematicBrushes main;

    public CommandHandler(SchematicBrushes main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (!(label.equalsIgnoreCase("sb") || label.equalsIgnoreCase("schematicbrushes"))) return false;
        if (!sender.hasPermission("schematicbrushes.admin")) return false;

        Player player = (sender instanceof Player) ? (Player) sender : null;
        // help,reload,give,get,list

        if (args.length == 0) {
            API.displayHelp(sender);
            return true;
        }

        // todo Look into better ways to separate sub-commands
        switch (args[0]) {
            case "reload":  // /sb reload
                main.reload();
                sender.sendMessage(API.color("&aSchematic Brushes reloaded"));
                break;
            case "give": { // /sb give PLAYER_NAME BRUSH_NAME
                if (args.length < 3) {
                    sender.sendMessage(API.color("&cUsage: /sb give PLAYER_NAME BRUSH_NAME"));
                    return false;
                }
                Player receiver = Bukkit.getPlayer(args[1]);
                Brush brush = API.getBrush(args[2]);
                if (receiver == null) {
                    sender.sendMessage(API.color("&cFailed to find player \"" + args[1] + "\"!"));
                    return false;
                } else if (brush == null) {
                    sender.sendMessage(API.color("&cFailed to find brush \"" + args[2] + "\"!"));
                    return false;
                }
                receiver.getInventory().addItem(brush.getItem());
                break;
            }
            case "get": { // /sb get BRUSH_NAME
                if (player == null) {
                    sender.sendMessage(API.color("&cYou must be a Player to get brushes."));
                    return false;
                }
                if (args.length != 2) {
                    Holder inventory = new Holder();
                    inventory.add(API.brushes.values().stream().map(Brush::getItem).distinct().toArray(ItemStack[]::new));
                    inventory.display(player);
                    return true;
                }
                
                Brush brush = API.getBrush(args[1]);
                if (brush == null) {
                    player.sendMessage(API.color("&cInvalid brush \"" + args[1] + "\". Use /sb list"));
                    return false;
                }

                player.getInventory().addItem(brush.getItem());
                break;
            }
            case "list":  // /sb list
                sender.sendMessage(API.color("&aLoaded Brushes:"));
                API.brushes.forEach((name, brush) -> sender.sendMessage(API.color("&a - " + name)));
                break;
            default:
                API.displayHelp(sender);
        }
        return true;
    }
}
