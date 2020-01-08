package me.cjcrafter.schematicbrushes.util;

import me.cjcrafter.schematicbrushes.API;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;

//@SuppressWarnings("ALL")
public class Config {

    /**
     * The appeal of using Maps over FileConfigurations
     * is that maps are much faster. By only using
     * FileConfigurations on startup and stop, this
     * Config class will save resources
     *
     * Each of these data types are in their own map
     * to avoid typecasting every time the user tries
     * to get something from the map.
     */
    private Map<String, Number>       numbers;
    private Map<String, String>       strings;
    private Map<String, Boolean>      booleans;
    private Map<String, ItemStack>    items;
    private Map<String, List<String>> lists;
    private Map<String, Object>       objects;

    /**
     * Initialize every map
     */
    public Config() {
        this.numbers  = new HashMap<>();
        this.strings  = new HashMap<>();
        this.booleans = new HashMap<>();
        this.items    = new HashMap<>();
        this.lists    = new HashMap<>();
        this.objects  = new HashMap<>();
    }

    /**
     * Adds each key and value from a FileConfiguration
     * individually into the map.
     *
     * @see org.bukkit.configuration.file.FileConfiguration
     * @param file File to read from
     */
    public Config add(@Nonnull FileConfiguration file) {
        API.getInstance().log(LogLevel.DEBUG, "Adding file: ", null);
        file.getKeys(true).forEach(key -> API.getInstance().log(LogLevel.DEBUG, key + ": " + file.get(key), null));
        file.getKeys(true)
                .stream()
                .filter(key -> {
                    if (containsKey(key)) {
                        API.getInstance().log(LogLevel.ERROR, "DUPLICATE KEY IN CONFIG: " + key, null);
                        return false;
                    } else return true;
                }).forEach(key -> set(key, file.get(key)));
        return this;
    }

    /**
     * Converts a List of unknown types
     * to a colored list of strings
     * @param list The List of unknown types
     * @return Converted list
     */
    @Nonnull
    private static List<String> convertList(Object list) {
        List<String> strings = new ArrayList<>();
        for (Object obj: (List<?>) list) {
            strings.add(API.color(obj.toString()));
        }
        return strings;
    }

    /**
     *
     * @param path
     */
    public boolean isNull(String path) {
        return !(
                strings.containsKey(path) ||
                numbers.containsKey(path) ||
                items.containsKey(path) ||
                lists.containsKey(path) ||
                booleans.containsKey(path) ||
                objects.containsKey(path)
        );
    }

    /**
     * Returns the double at the given key
     * @param path Key
     * @return value
     */
    public Number getNumber(String path) {
        if (numbers.get(path) != null)
            return numbers.get(path);
        else return 0.0;
    }

    /**
     * Returns the String at the given key
     * @param path Key
     * @return value
     */
    public String getString(String path) {
        if (strings.get(path) != null)
            return strings.get(path);
        else return "";
    }

    /**
     * Returns the boolean at the given key
     * @param path Key
     * @return value
     */
    public boolean getBoolean(String path) {
        if (booleans.get(path) != null)
            return booleans.get(path);
        else return false;
    }

    /**
     * Returns the ItemStack at the given key
     * @param path Key
     * @return value
     */
    public ItemStack getItem(String path) {
        if (items.get(path) != null)
            return items.get(path);
        else return new ItemStack(Material.AIR);
    }

    /**
     * Returns the List of Strings at the given key
     * @param path Key
     * @return value
     */
    public List<String> getList(String path) {
        if (lists.get(path) != null)
            return lists.get(path);
        else return new ArrayList<>();
    }

    /**
     * Adds the key and value to the value's
     * corresponding map
     *
     * @param key   Path to value
     * @param value Value
     */
    public void set(String key, Object value) {
        if (value instanceof Number)
            numbers.put(key, ((Number) value).doubleValue());
        else if (value instanceof String)
            strings.put(key, (String) value);
        else if (value instanceof Boolean || "true".equals(value) || "false".equals(value))
            booleans.put(key, Boolean.valueOf(value.toString()));
        else if (value instanceof ItemStack)
            items.put(key, (ItemStack) value);
        else if (value instanceof List<?>)
            lists.put(key, convertList(value));
        else
            objects.put(key, value);
    }

    /**
     * Saves all Maps back into a given File
     * @param file File to save to
     */
    public void save(@Nonnull File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        add(config);
        config.getKeys(true).forEach(key -> config.set(key, null));

        numbers.forEach(config::set);
        strings.forEach(config::set);
        booleans.forEach(config::set);
        items.forEach(config::set);
        lists.forEach(config::set);
        objects.forEach(config::set);

        try {
            config.save(file);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save file " + file, ex);
        }
    }

    /**
     *
     * @param path
     * @param key
     * @param value
     */
    public void add(String path, String key, Object value) {
        for (int i = 0;; i++) {
            if (isNull(path + "." + i + "." + key)) {
                set(path + "." +  i + "." + key, value);
                break;
            }
        }
    }

    /**
     * Shorthand to check if any map contains
     * the given key
     *
     * @param key Key
     * @return If any map containsKey
     */
    public boolean containsKey(String key) {
        if (numbers.containsKey(key)) return true;
        else if (strings.containsKey(key)) return true;
        else if (booleans.containsKey(key)) return true;
        else if (items.containsKey(key)) return true;
        else if (lists.containsKey(key)) return true;
        else return objects.containsKey(key);
    }

    /**
     * Shorthand to clear all maps
     */
    public void clear() {
        numbers.clear();
        strings.clear();
        booleans.clear();
        items.clear();
        lists.clear();
        objects.clear();
    }

    /**
     * Loops for each (key, value) in each map.
     * A key from the map is "valid" if it:
     *   1. Has at least 1 more '.'s then `base`
     *   2. Map#containsKey(`base`)
     *
     * @param base     The key to count '.' from
     * @param consumer Action to perform
     */
    public void forEach(String base, BiConsumer<String, Object> consumer, boolean deep) {
        forEach(numbers, base, consumer, deep);
        forEach(strings, base, consumer, deep);
        forEach(booleans, base, consumer, deep);
        forEach(items, base, consumer, deep);
        forEach(lists, base, consumer, deep);
        forEach(objects, base, consumer, deep);
    }

    private void forEach(Map<String, ?> map, String base, BiConsumer<String, Object> consumer, boolean deep) {
        int dots = countDots(base);
        map.forEach((key, value) -> {
            API.getInstance().log(LogLevel.DEBUG, "Deep: " + deep, null);
            API.getInstance().log(LogLevel.DEBUG, "Contains: " + key.contains(base), null);
            API.getInstance().log(LogLevel.DEBUG, "Dots: " + dots + " ? " + countDots(key), null);
            if (deep && key.contains(base) && countDots(key) > dots) {
                consumer.accept(key, value);
                API.getInstance().log(LogLevel.DEBUG, "FOREACH:" + key + ": " + value, null);
            }
            else if (!deep && containsKey(base) && countDots(key) == dots + 1) {
                consumer.accept(key, value);
                API.getInstance().log(LogLevel.DEBUG, "FOREACH:" + key + ": " + value, null);
            }
        });
    }

    private static int countDots(String string) {
        return (int) string.chars().filter(c -> c == '.').count();
    }
}