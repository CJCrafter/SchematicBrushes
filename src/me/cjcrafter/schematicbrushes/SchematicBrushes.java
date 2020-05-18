package me.cjcrafter.schematicbrushes;

import me.cjcrafter.core.file.Configuration;
import me.cjcrafter.core.file.SeparatedConfig;
import me.cjcrafter.core.utils.DebugUtils;
import me.cjcrafter.core.utils.ReflectionUtils;
import me.cjcrafter.schematicbrushes.commands.SchematicBrushesMainCommand;
import me.cjcrafter.schematicbrushes.listeners.InteractListener;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class SchematicBrushes extends JavaPlugin {

    private static Configuration config;
    private static SchematicBrushes instance;

    @Override
    public void onLoad() {
        instance = this;
        DebugUtils.logger = getLogger();

        saveDefaultConfig();
        reloadConfig();
        config = new SeparatedConfig();
        config.add(getConfig(), false);
        DebugUtils.level = config.getInt("Debug_Level", 2);
    }

    @Override
    public void onEnable() {

        // Register brushes. Must be done in onEnable and not onLoad
        config.forEach("Brushes", (key, value) -> new Brush(key.split("\\.")[1]), false);

        Method getCommandMap = ReflectionUtils.getMethod(ReflectionUtils.getCBClass("CraftServer"), "getCommandMap");
        SimpleCommandMap simpleCommandMap = (SimpleCommandMap) ReflectionUtils.invokeMethod(getCommandMap, Bukkit.getServer());

        simpleCommandMap.register("SchematicBrushes", new SchematicBrushesMainCommand());

        //getServer().getPluginManager().registerEvents(new ChunkLoadListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
    }

    public void onReload() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        Brush.brushes.clear();
        config.clear();

        saveDefaultConfig();
        reloadConfig();
        config.add(getConfig(), false);
        DebugUtils.level = config.getInt("Debug_Level", 2);

        config.forEach("Brushes", (key, value) -> new Brush(key.split("\\.")[1]), false);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        Brush.brushes.clear();
        config = null;
        instance = null;
    }

    public static Configuration getConfiguration() {
        return config;
    }

    public static SchematicBrushes getInstance() {
        return instance;
    }
}
