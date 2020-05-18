package me.cjcrafter.core;

import me.cjcrafter.core.utils.DebugUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckCore extends JavaPlugin {

    @Override
    public void onLoad() {
        DebugUtils.logger = getLogger();
    }

    @Override
    public void onEnable() {

    }

    public void onReload() {

    }

    @Override
    public void onDisable() {

    }
}
