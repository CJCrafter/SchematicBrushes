package me.cjcrafter.schematicbrushes;

import me.cjcrafter.schematicbrushes.commands.CommandHandler;
import me.cjcrafter.schematicbrushes.util.Config;
import me.cjcrafter.schematicbrushes.util.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SchematicBrushes extends JavaPlugin implements Listener {

    public Config config;

    @Override
    public void onEnable() {
        config = new Config();
        new API(this);
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            log(LogLevel.ERROR, "Your WorldEdit is not found!", null);
            getServer().getPluginManager().disablePlugin(this);
        }
        saveDefaultConfig();
        reloadConfig();
        config.add(super.getConfig());
        config.forEach("Brushes", (key, value) -> API.brushes.put(key.split("\\.")[1], new Brush(key)), false);

        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("schematicbrushes")).setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        config.clear();
        API.brushes.clear();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        sender.sendMessage("args=" + Arrays.toString(args));
        if (args[0].equals("give")) {
            if (args.length < 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            else if (args.length < 3) return new ArrayList<>(API.brushes.keySet());
        }
        else if (args[0].equals("get")) {
            if (args.length < 2) return new ArrayList<>(API.brushes.keySet());
        }
        else return getList("reload", "get", "give", "list");
        return getList("");
    }

    private List<String> getList(String...strings) {
        return new ArrayList<>(Arrays.asList(strings));
    }

    public void reload() {
        getServer().getPluginManager().disablePlugin(this);
        getServer().getPluginManager().enablePlugin(this);
    }

    @EventHandler
    public void onPaint(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("schematicbrushes.use")) return;

        ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (meta == null || meta.getDisplayName().split("~").length != 2) return;

        Brush brush = API.getBrush(meta.getDisplayName().split("~")[1]);
        if (brush == null) return;

        brush.paste(e.getPlayer(), e.getPlayer().getTargetBlock(null, (int) API.getDouble("Max_Brush_Distance")).getLocation());
    }

    public void log(LogLevel level, String msg) {
        log(level, msg, null);
    }

    public void log(LogLevel level, String msg, @Nullable Throwable error) {
        if (!level.isValidLevel(API.getInt("Debug_Level"))) return;

        switch (level.name()) {
            case "DEBUG": case "INFO":
                getLogger().log(Level.INFO, msg);
                break;
            case "WARN":
                getLogger().log(Level.WARNING, msg);
                break;
            case "ERROR":
                getLogger().log(Level.SEVERE, msg, error);
                break;
        }
    }
}
