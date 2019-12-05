package me.cjcrafter.schematicbrushes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

class API {

    static Map<String, Brush> brushes = new HashMap<>();
    static Map<String, Object> config = new HashMap<>();
    private static SchematicBrushes main;
    
    API(SchematicBrushes main) {
        API.main = main;
    }

    static String getString(String path) {
        if (config.get(path) != null) {
            return (String) config.get(path);
        }
        return "";
    }

    static boolean getBool(String path) {
        if (config.get(path) != null)  {
            return (boolean) config.get(path);
        }
        return false;
    }

    static double getDouble(String path) {
        if (config.get(path) != null) {
            return (double) config.get(path);
        }
        return 0.0;
    }

    @SuppressWarnings("unchecked")
    static List<String> getList(String path) {
        if (config.get(path) != null) {
            return (List<String>) config.get(path);
        }
        return new ArrayList<>();
    }

    static void forEach(BiConsumer<String, Object> consumer) {
        int count = countDots("Brushes");
        config.forEach((key, value) -> {
            if (count + 1 == countDots(key) && key.contains("Brushes"))
                consumer.accept(key, value);
        });
    }

    private static int countDots(String string) {
        return (int) string.chars().filter(c -> c == '.').count();
    }

    static void add(FileConfiguration file) {
        for (String key : file.getKeys(true)) {
            Object obj = file.get(key);

            if (obj instanceof Number)
                config.put(key, ((Number) obj).doubleValue());
            else if (obj instanceof String)
                config.put(key, color(obj.toString()));
            else if (obj instanceof Boolean || "true".equals(obj) || "false".equals(obj))
                config.put(key, Boolean.valueOf(obj.toString()));
            else if (obj instanceof List<?>)
                config.put(key, convertList(obj));
            else
                config.put(key, null);
        }
    }
    
    private static List<String> convertList(Object object) {
        List<String> strings = new ArrayList<>();
        for (Object obj: (List<?>) object) {
            strings.add(color(obj.toString()));
        }
        return strings;
    }
    
    static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    static void displayHelp(CommandSender sender) {
        sender.sendMessage(color("&6SchematicBrushes, By: CJCrafter"));
        sender.sendMessage(color("&a/sb get <Schematic_Name>"));
        sender.sendMessage(color("&a/sb give <Player> <Schematic_Name>"));
        sender.sendMessage(color("&a/sb list"));
        sender.sendMessage(color("&a/sb reload"));
    }

    static Brush getBrush(String name) {
        return brushes.get(name);
    }

    static Clipboard getSchematic(String name) {
        File file = new File(main.getServer().getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics" + File.separator + name + ".schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException ex) {
            System.err.println("[SchematicBrushes] There was an error loading schematic \"" + name + "\" at " + file.getPath());
        }
        return null;
    }
}
