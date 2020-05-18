package me.cjcrafter.schematicbrushes;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.cjcrafter.core.file.Configuration;
import me.cjcrafter.core.utils.*;
import me.cjcrafter.schematicbrushes.utils.SchematicLoader;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Brush {

    private static EditSessionFactory factory = WorldEdit.getInstance().getEditSessionFactory();
    public static Map<String, Brush> brushes = new HashMap<>();
    private static Configuration config = SchematicBrushes.getConfiguration();

    private String name;
    private LinkedHashMap<Clipboard, Double> schematics;  // The Double should be [0, 1] representing the chance to be selected

    public Brush(String name) {
        this.name = name;
        this.schematics = new LinkedHashMap<>();

        // Add all the schematics to the map
        for (String str : config.getSet("Brushes." + name + ".Schematics")) {
            String[] split = str.split("~");

            // If no chance was specified, put a 100% chance
            if (split.length != 2) {
                schematics.put(SchematicLoader.getSchematic(str), 1.0);
            } else {
                schematics.put(SchematicLoader.getSchematic(split[0]), Double.parseDouble(split[1]));
            }
        }

        DebugUtils.log(Log.DEBUG, "Registered brush " + name);
        brushes.put(name, this);
    }

    public void click(Player player, Location loc) {
        List<Location> locations = new ArrayList<>();
        DebugUtils.log(Log.DEBUG, "Clicked " + name);

        if (config.getBool("Brushes." + name + ".Branch.Enabled")) {
            branch(loc, locations, config.getInt("Brushes." + name + ".Branch.Depth", 1));
        } else {
            locations.add(loc);
        }

        try (EditSession session = factory.getEditSession(new BukkitWorld(loc.getWorld()), -1)) {

            MessageUtils.message(player, "Pasting " + locations.size() + " schematics.");
            for (Location found : locations) {
                paste(session, found);
            }

            // Players remember the paste for //undo
            LocalSession local = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
            if (local == null) {
                DebugUtils.log(Log.WARN, "Failed to find player \"" + player.getName()
                        + "\" on the server...Did they log off?");
                return;
            }
            local.remember(session);
        } catch (WorldEditException ex) {
            DebugUtils.log(Log.ERROR, "WorldEditException occurred while pasting.", ex);
        }
    }

    private void branch(Location base, List<Location> parentLocations, int depth) {
        List<Location> locations = new ArrayList<>();

        int min = config.getInt("Brushes." + name + ".Branch.Min_Schematics");
        int max = config.getInt("Brushes." + name + ".Branch.Max_Schematics");
        int toFind = NumberUtils.random(min, max);

        double range = config.getDouble("Brushes." + name + ".Branch.Range");
        boolean isBlacklist = !config.getBool("Brushes." + name + ".Branch.Block_Whitelist");
        Set<String> validationSet = config.getSet("Brushes." + name + ".Branch.Valid_Blocks");

        int maxChecks = config.getInt("Branch_Max_Checks");

        while (locations.size() < toFind && maxChecks-- > 0) {
            int x = (int) (NumberUtils.random(-range / 2.0, range / 2.0) + base.getX());
            int z = (int) (NumberUtils.random(-range / 2.0, range / 2.0) + base.getZ());
            final Location found = new Location(base.getWorld(), x, base.getY(), z);

            // Make sure there is enough distance in between the other found points
            double minDistance = config.getDouble("Brushes." + name + ".Branch.Space_Between_Schematics");
            if (
                    locations.stream().anyMatch(location -> location.distance(found) < minDistance) ||
                    parentLocations.stream().anyMatch(location -> location.distance(found) < minDistance)
            ) {
                continue;
            }

            // Apply ground if enabled
            if (config.getBool("Brushes." + name + ".Branch.Ground.Lock")) {
                Block ground = getGround(found.getWorld(), found.getBlockX(), found.getBlockY(), found.getBlockZ());
                if (ground == null) continue;
                found.setY(ground.getY());
            }

            // Apply blacklist/whitelist
            if (validationSet.contains(found.getBlock().getType().name()) == isBlacklist) {
                continue;
            }

            locations.add(found);
        }

        parentLocations.addAll(locations);

        if (--depth > 0) {
            for (Location next : new ArrayList<>(locations)) {
                locations.remove(next);

                // Apply the distance for branching outwards
                Vector vector = next.clone().subtract(base).toVector().setY(0);
                vector.multiply(config.getDouble("Brushes." + name + ".Branch.Distance_Multiplier"));
                next.add(vector);

                DebugUtils.log(Log.DEBUG, "Creating branch at " + next.getBlock());
                branch(next, parentLocations, depth);
            }
        }
    }


    /**
     * Pastes a random schematic at the given <code>Location</code>
     *
     * The given <code>EditSession</code> is used to handle operations
     * of multiple pastes
     *
     * @param session The editsession involved in pasting
     * @param loc The location to paste at
     * @throws WorldEditException If an error occurs while pasting
     */
    public void paste(@Nonnull EditSession session, @Nonnull Location loc) throws WorldEditException {

        // Setup the clipboard
        Clipboard schematic = getRandomSchematic();
        if (schematic == null) return;
        ClipboardHolder holder = new ClipboardHolder(schematic);
        holder.setTransform(getRotation());

        // Build the operation
        Operation operation = holder
                .createPaste(session)
                .to(BlockVector3.at(
                        loc.getX() + config.getDouble("Brushes." + name + ".X"),
                        loc.getY() + config.getDouble("Brushes." + name + ".Y"),
                        loc.getZ() + config.getDouble("Brushes." + name + ".Z")))
                .ignoreAirBlocks(config.getBool("Brushes." + name + ".Ignore_Air"))
                .copyBiomes(config.getBool("Brushes." + name + ".Copy_Biomes"))
                .copyEntities(config.getBool("Brushes." + name + ".Copy_Entities"))
                .build();

        // Paste the operation
        Operations.complete(operation);
    }

    /**
     * Gets a random schematic based on it's chance
     * in the schematics map
     *
     * @return The random schematic
     */
    @Nullable
    private Clipboard getRandomSchematic() {
        int maxTries = config.getInt("Schematic_Search_Max_Tries", 20);
        Clipboard[] keys = schematics.keySet().toArray(new Clipboard[0]);

        do {
            // Get a random key from the map
            int random = NumberUtils.random(0, keys.length - 1);
            Clipboard key = keys[random];

            // Test the chance for the schematic to paste
            if (NumberUtils.chance(schematics.get(key))) {
                return key;
            }
        } while (--maxTries > 0);

        DebugUtils.log(Log.DEBUG, "Failed to find a schematic in the given number of tries");
        return null;
    }

    /**
     * Gets a rotation based on this <code>Brush</code>'s
     * configuration. If -1 is given, then the rotation is
     * random
     *
     * @return The rotation
     */
    private AffineTransform getRotation() {
        double x = config.getDouble("Brushes." + name + ".Rotate_X", 0);
        double y = config.getDouble("Brushes." + name + ".Rotate_Y", 0);
        double z = config.getDouble("Brushes." + name + ".Rotate_Z", 0);
        return new AffineTransform()
                .rotateX(((int) x == -1) ? NumberUtils.random(0, 4) * 90 : x)
                .rotateY(((int) y == -1) ? NumberUtils.random(0, 4) * 90 : y)
                .rotateZ(((int) z == -1) ? NumberUtils.random(0, 4) * 90 : z);
    }

    /**
     * Gets the "Item Value" of this brush, for users
     * to paste their schematics
     *
     * @return This brush
     */
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("Brushes." + name + ".Material")));
        ItemMeta meta = item.getItemMeta();
        assert meta != null;    // Although meta is never null (Outdated API), this removes warnings

        // Make it look pretty
        meta.setDisplayName(StringUtils.color("&aSchematicBrushes~" + name));
        meta.setLore(new ArrayList<>(config.getSet("Brushes." + name + ".Schematics")));
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Finds the nearest ground at the given location (within
     * the configured bounds)
     *
     * @param world The nonnull world to look in
     * @param x The x component/location
     * @param y The y component/location
     * @param z The z component/location
     * @return The found block
     */
    @Nullable
    private Block getGround(@Nonnull World world, int x, int y, int z) {
        int bound = config.getInt("Brushes." + name + ".Branch.Ground.Bound");
        int lower = Math.max(y - bound, 0);
        Set<String> ignored = config.getSet("Brushes." + name + ".Branch.Ground.Ignore_Blocks");

        for (y = Math.min(y + bound, 255); y > lower; y--) {
            Block block = world.getBlockAt(x, y, z);

            // If the block's type is not on the blacklist,
            // then we have found our location
            if (!ignored.contains(block.getType().name())) {
                return block;
            }
        }
        return null;
    }

    /**
     * Gets brushes by name
     *
     * @param name The name of the brush
     * @return Brush with the given name (If available)
     */
    public static Brush forName(String name) {
        return brushes.get(name);
    }
}
