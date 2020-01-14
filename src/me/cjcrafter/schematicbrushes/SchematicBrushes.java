package me.cjcrafter.schematicbrushes;

import me.cjcrafter.schematicbrushes.commands.CommandHandler;
import me.cjcrafter.schematicbrushes.commands.TabCompleter;
import me.cjcrafter.schematicbrushes.util.Config;
import me.cjcrafter.schematicbrushes.util.LogLevel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class SchematicBrushes extends JavaPlugin implements Listener {

    Config config;

    @Override
    public void onEnable() {
        config = new Config();
        new API(this);
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            API.log(LogLevel.ERROR, "WorldEdit is not installed!", null);
            API.log(LogLevel.ERROR, "SchematicBrushes cannot function without WorldEdit!");
            API.log(LogLevel.ERROR, "Disabling Schematic Brushes");
            getServer().getPluginManager().disablePlugin(this);
        }
        handleDefaults();
        config.forEach("Brushes", (key, value) -> API.brushes.put(key.split("\\.")[1], new Brush(key)), false);

        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("schematicbrushes")).setExecutor(new CommandHandler(this));
        Objects.requireNonNull(getCommand("schematicbrushes")).setTabCompleter(new TabCompleter());
    }

    @Override
    public void onDisable() {
        config.clear();
        API.brushes.clear();
    }

    public void reload() {
        getServer().getPluginManager().disablePlugin(this);
        getServer().getPluginManager().enablePlugin(this);
    }
    
    private void handleDefaults() {
        saveDefaultConfig();
        reloadConfig();
        config.add(super.getConfig(), false);
        setDefault("Max_Brush_Distance", 75);
        setDefault("Scatter_Max_Checks", 1000);
        setDefault("Build_Height", 255);
        setDefault("Debug_Level", 1);
        config.save(new File(getDataFolder(), "config.yml"));
        reloadConfig();
    }
    
    private void setDefault(String key, Object value) {
        if (!config.containsKey(key)) config.set(key, value);
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
}
