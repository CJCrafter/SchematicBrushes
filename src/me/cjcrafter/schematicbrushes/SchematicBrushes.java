package me.cjcrafter.schematicbrushes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicBrushes extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            System.err.println("CRITICAL ERROR !!! WorldEdit not found on server!");
            System.err.println("Disabling SchematicBrushes to avoid more errors!");
            getServer().getPluginManager().disablePlugin(this);
        }
        new API(this);
        API.add(getConfig());
        API.forEach((key, value) -> API.brushes.put(key.split("\\.")[1], new Brush(key)));

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        API.config.clear();
        API.brushes.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            if (args.length == 0) {
                API.displayHelp(sender);
                return false;
            }
            if (sender instanceof Player && args[0].equals("get") && args.length >= 2) {
                ((Player) sender).getInventory().addItem(API.getBrush(args[1]).getItem());
            } else if (args[0].equals("reload")) {
                reload();
                sender.sendMessage(API.color("&a[SchematicBrushes] Plugin successfully restarted."));
            } else if (args[0].equals("give") && args.length >= 3) {
                Bukkit.getPlayer(args[1]).getInventory().addItem(API.getBrush(args[2]).getItem());
            } else if (args[0].equals("list")) {
                API.brushes.forEach((key, value) -> sender.sendMessage(key));
            } else API.displayHelp(sender);
        } catch (Exception ex) {
            sender.sendMessage("Some error occurred...");
            ex.printStackTrace();
        }
        return true;
    }

    private void reload() {
        getServer().getPluginManager().disablePlugin(this);
        getServer().getPluginManager().enablePlugin(this);
    }

    @EventHandler
    public void onPaint(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("schematicbrushes.use")) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().split("~").length != 2) return;
        if (API.getBrush(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().split("~")[1]) == null) return;

        API.getBrush(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().split("~")[1])
                .paste(e.getPlayer(), e.getPlayer().getTargetBlock(null, (int) API.getDouble("Max_Brush_Distance")).getLocation());
    }
}
