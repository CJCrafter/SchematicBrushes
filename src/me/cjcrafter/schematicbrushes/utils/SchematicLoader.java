package me.cjcrafter.schematicbrushes.utils;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import me.cjcrafter.core.utils.DebugUtils;
import me.cjcrafter.core.utils.Log;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicLoader {

    private static File schematicsFolder = null;

    public static Clipboard getSchematic(String name) {
        if (schematicsFolder == null) {
            File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit").getDataFolder();
            schematicsFolder = new File(dataFolder, "schematics");
        }

        // Make the plugin smarter, can fix user error
        if (!name.contains(".schem")) name += ".schem";
        File schematic = new File(schematicsFolder, name);

        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        assert format != null;
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            return reader.read();
        } catch (IOException ex) {
            DebugUtils.log(Log.ERROR, "Failed to load schematic \"" + name + "\", did you name it correctly?");
        }
        return null;
    }
}
