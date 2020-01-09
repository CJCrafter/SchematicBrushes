package me.cjcrafter.schematicbrushes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class API {

    public static Map<String, Brush> brushes = new HashMap<>();
    private static SchematicBrushes main;

    API(SchematicBrushes main) {
        API.main = main;
    }

    public static String getString(String path) {
        return main.config.getString(path);
    }

    public static boolean getBool(String path) {
        return main.config.getBoolean(path);
    }

    public static double getDouble(String path) {
        return main.config.getNumber(path).doubleValue();
    }

    public static int getInt(String path) {
        return main.config.getNumber(path).intValue();
    }

    public static List<String> getList(String path) {
        return main.config.getList(path);

    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static SchematicBrushes getInstance() {
        return main;
    }

    public static void displayHelp(CommandSender sender) {
        sender.sendMessage(color("&6SchematicBrushes, By: CJCrafter"));
        sender.sendMessage(color("&a/sb get <Schematic_Name>"));
        sender.sendMessage(color("&a/sb give <Player> <Schematic_Name>"));
        sender.sendMessage(color("&a/sb list"));
        sender.sendMessage(color("&a/sb reload"));
    }

    public static Brush getBrush(String name) {
        return brushes.get(ChatColor.stripColor(name));
    }

    static Clipboard getSchematic(String name) {
        if (!name.contains(".schem")) name += ".schem";
        File file = new File(main.getServer().getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics" + File.separator + name);
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException ex) {
            System.err.println("[SchematicBrushes] There was an error loading schematic \"" + name + "\" at " + file.getPath());
        }
        return null;
    }
}
