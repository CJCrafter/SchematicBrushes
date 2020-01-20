package me.cjcrafter.schematicbrushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.cjcrafter.schematicbrushes.util.LogLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class Brush {

    private static final Random rand = new Random();

    private String name;
    private List<Clipboard> schematics;

    Brush(String name) {
        this.name = name.split("\\.")[1];
        this.schematics = API.getList("Brushes." + this.name + ".Schematics")
                .stream()
                .map(API::getSchematic)
                .collect(Collectors.toList());
    }

    /**
     * Called whenever a player who has permission clicks at a block with this brush.
     * Determines whether the brush is a scatter or normal brush, then proceeds to
     * find locations to paste schematic(s)
     *
     * TODO Separate to click and scatter methods
     *
     * @param player Player to save Clipboard to (For //undo)
     * @param loc Where the player clicked
     */
    void paste(Player player, Location loc) {
        if (!API.getBool("Brushes." + name + ".Scatter.Enabled")) {

            paste(getRandomSchematic(), player, loc);
            return;
        }

        int min = API.getInt("Brushes." + name + ".Scatter.Min_Schematics");
        int max = API.getInt("Brushes." + name + ".Scatter.Max_Schematics");
        int total = (max - min <= 0) ? max : rand.nextInt(1 + max - min) + min;
        player.sendMessage(API.color("&aPasting " + total + " schematics."));

        double range = API.getDouble("Brushes." + name + ".Scatter.Range");

        List<Location> locations = new ArrayList<>();
        int i = 0;
        while (locations.size() < total && i++ < API.getInt("Scatter_Max_Checks")) {
            double x = rand.nextDouble() * range + loc.getX() - range / 2;
            double z = rand.nextDouble() * range + loc.getZ() - range / 2;
            Location currentLocation = new Location(loc.getWorld(), x, loc.getY(), z);

            // Any conditions checking if a Location is a "valid random location"
            // should go here
            if (locations.stream()
                    .anyMatch(iterator -> iterator.distance(currentLocation) < API.getDouble("Brushes." + name + ".Scatter.Space_Between_Schematics")))
                continue;

            if (API.getBool("Brushes." + name + ".Scatter.Ground.Lock")) getGround(currentLocation);

            boolean isOnList = API.getList("Brushes." + name + ".Scatter.Valid_Blocks")
                    .contains(currentLocation.getBlock().getType().name());
            if (isOnList != API.getBool("Brushes." + name + ".Scatter.Block_Whitelist")) {
                continue;
            }

            locations.add(currentLocation);
        }
        locations.forEach(location -> paste(getRandomSchematic(), player, location));
    }

    /**
     * Gets the highest possible ground level within <Code>bound</Code>
     * @param loc The location for the x,z values, and to set the y
     */
    private void getGround(Location loc) {
        API.log(LogLevel.DEBUG, "Attempting to find ground at " + loc);
        int bound = API.getInt("Brushes." + name + ".Scatter.Ground.Bound");
        int lower = Math.max(loc.getBlockY() - bound, 0);
        int upper = Math.min(loc.getBlockY() + bound, API.getInt("Build_Height"));
        API.log(LogLevel.DEBUG, "Checking from y" + upper + " to y" + lower);
        
        for (int y = upper; y > lower; y--) {
            loc.setY(y);
            Block block = loc.getBlock();
            if (API.getList("Brushes." + name + ".Scatter.Ground.Ignore_Blocks").contains(block.getType().name())) {
                API.log(LogLevel.DEBUG, "y" + y + " is " + block.getType().name() + "...Blocked..." + upper + ">" + lower);
                continue;
            }

            API.log(LogLevel.DEBUG, "&aBreaking at y= " + y + "(" + block.getType().name() + ")", null);
            break;
        }
    }

    private void paste(Clipboard schematic, Player player, Location loc) {
        API.log(LogLevel.DEBUG, "Pasting at " + loc);
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()), -1)) {
            ClipboardHolder holder = new ClipboardHolder(schematic);
            holder.setTransform(getRotation());
            Operation operation = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(
                            loc.getX() + API.getDouble("Brushes." + name + ".X"),
                            loc.getY() + API.getDouble("Brushes." + name + ".Y"),
                            loc.getZ() + API.getDouble("Brushes." + name + ".Z")))
                    .ignoreAirBlocks(API.getBool("Brushes." + name + ".Ignore_Air"))
                    .build();
            Operations.complete(operation);
            Objects.requireNonNull(WorldEdit.getInstance().getSessionManager().findByName(player.getName())).remember(editSession);
        } catch (WorldEditException e) {
            API.log(LogLevel.ERROR, "Schematic \"" + schematic + "\" failed to paste", e);
        } catch (NullPointerException e) {
            API.log(LogLevel.WARN, "Player \"" + player.getName() +
                    "\" has left the server while painting.", e);
        }
    }

    /**
     * Gets a rotation for this brush based on configuration
     * Remember that -1 is a random number
     * @return A rotation for a ClipboardHolder
     */
    private AffineTransform getRotation() {
        double x = API.getDouble("Brushes." + name + ".Rotate_X");
        double y = API.getDouble("Brushes." + name + ".Rotate_Y");
        double z = API.getDouble("Brushes." + name + ".Rotate_Z");
        return new AffineTransform()
                .rotateX(((int) x == -1) ? rand.nextInt(4) * 90 : x)
                .rotateY(((int) y == -1) ? rand.nextInt(4) * 90 : y)
                .rotateZ(((int) z == -1) ? rand.nextInt(4) * 90 : z);
    }

    private Clipboard getRandomSchematic() {
        return schematics.get(rand.nextInt(schematics.size()));
    }

    /**
     * Returns the brush item (When using /sb give, or /sb get)
     * @return The brush item
     */
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(API.getString("Brushes." + name + ".Material"))));
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(API.color("&aSchematicBrushes~" + name));
        meta.setLore(API.getList("Brushes." + this.name + ".Schematics"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String toString() {
        return "Brush[" +
                "name=" + name +
                ",schematics=" + schematics +
                "]";
    }
    
    public static void main(String[] args) {
        // Use for testing speed of different maps
        final int times = 100;
    
        Map<String, Double> hash = new HashMap<>();
        Map<String, Double> linked = new LinkedHashMap<>();
        
        long fillHashMap = 0;
        long fillLinkedHashMap = 0;
        
        long iterateHashMap = 0;
        long iterateLinkedHashMap = 0;
        
        long accessHashMap = 0;
        long accessLinkedHashMap = 0;
        
        for (int i = 0; i < times; i++) {
            fillHashMap += time(() -> fillMap(hash, 1_000_000));
            fillLinkedHashMap += time(() -> fillMap(linked, 1_000_000));
            
            iterateHashMap += time(() -> hash.forEach((key, value) -> value++));
            iterateLinkedHashMap += time(() -> linked.forEach((key, value) -> value++));
            
            accessHashMap += time(() -> {
                for (int j = 0; j < 100_000; j++) {
                    hash.get(j + "");
                }
            });
            accessLinkedHashMap += time(() -> {
                for (int j = 0; j < 100_000; j++) {
                    linked.get(j + "");
                }
            });
        }
        
        fillHashMap /= times;
        fillLinkedHashMap /= times;
        
        iterateHashMap /= times;
        iterateLinkedHashMap /= times;
        
        accessHashMap /= times;
        accessLinkedHashMap /= times;
        
        System.out.printf("Filling HashMap      : %s %n", fillHashMap);
        System.out.printf("Filling LinkedHashMap: %s %n %n", fillLinkedHashMap);
        System.out.printf("Iterati HashMap      : %s %n", iterateHashMap);
        System.out.printf("Iterati LinkedHashMap: %s %n %n", iterateLinkedHashMap);
        System.out.printf("Accessi HashMap      : %s %n", accessHashMap);
        System.out.printf("Accessi LinkedHashMap: %s %n", accessLinkedHashMap);
    }
    
    public static void fillMap(Map<String, Double> map, int bound) {
        for (int i = 0; i < bound; i++) {
            map.put(i + "", Math.random());
        }
    }
    
    private static long time(Action action) {
        long before = System.currentTimeMillis();
        action.doSomething();
        return System.currentTimeMillis() - before;
    }
    
    private interface Action {
        void doSomething();
    }
}