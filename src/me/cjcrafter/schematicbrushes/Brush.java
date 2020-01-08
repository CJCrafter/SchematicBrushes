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
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Brush {

    private static final Random rand = new Random();

    private String name;
    private List<Clipboard> schematics;

    Brush(String name) {
        this.name = name.split("\\.")[1];
        this.schematics = new ArrayList<>();

        API.getList("Brushes." + this.name + ".Schematics").forEach(str -> schematics.add(API.getSchematic(str)));
    }

    public void paste(Player player, Location loc) {
        if (!API.getBool("Brushes." + name + ".Scatter.Enabled")) {
            paste(schematics.get(rand.nextInt(schematics.size())), player, loc);
            return;
        }

        int min = API.getInt("Brushes." + name + ".Scatter.Min_Schematics");
        int max = API.getInt("Brushes." + name + ".Scatter.Max_Schematics");
        int total = (max - min <= 0) ? max : rand.nextInt(1 + max - min) + min;
        player.sendMessage("Going to paste " + total + "schematics");

        double range = API.getDouble("Brushes." + name + ".Scatter.Range");

        List<Location> locations = new ArrayList<>();
        int i = 0;
        while (locations.size() < total && i++ < API.getInt("Scatter_Max_Checks")) {
            double x = rand.nextDouble() * range + loc.getX() - range / 2;
            double y = loc.getY();
            double z = rand.nextDouble() * range + loc.getZ() - range / 2;
            Location currentLocation = new Location(loc.getWorld(), x, y, z);

            // Any conditions checking if a Location is a "valid random location"
            // should go here
            if (locations
                    .stream()
                    .anyMatch(iterator -> iterator.distance(currentLocation) < API.getDouble("Brushes." + name + ".Scatter.Space_Between_Schematics")))
                continue;

            if (API.getBool("Brushes." + name + ".Scatter.Ground.Lock")) {
                getGround(currentLocation);
            }
            locations.add(currentLocation);
        }

        locations.forEach(location -> paste(schematics.get(rand.nextInt(schematics.size())), player, location));
    }

    private void getGround(Location loc) {
        API.getInstance().log(LogLevel.DEBUG, "&eAttempting to find ground at " + loc);
        int bound = API.getInt("Brushes." + name + ".Scatter.Ground.Bound");
        int lower = Math.max(loc.getBlockY() - bound, 0);
        int upper = Math.min(loc.getBlockY() + bound, 255);

        for (int y = upper; y > lower; y--) {
            Block block = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            API.getInstance().log(LogLevel.DEBUG, "Checking " + block.getLocation(), null);
            if (API.getList("Brushes." + name + ".Scatter.Ground.Ignore_Blocks")
                    .stream()
                    .anyMatch(s -> s.equals(block.getType().name()))
            ) {
                API.getInstance().log(LogLevel.DEBUG, "y" + y + " is " + block.getType().name() + "...Blocked..." + upper + ">" + lower);
                continue;
            }

            API.getInstance().log(LogLevel.DEBUG, "&aBreaking at y= " + y + "(" + block.getType().name() + ")", null);
            loc.setY(y);
            break;
        }
    }

    private void paste(Clipboard schematic, Player player, Location loc) {
        player.sendMessage(API.color("&aPasting at: (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
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
            WorldEdit.getInstance().getSessionManager().findByName(player.getName()).remember(editSession);
        } catch (WorldEditException e) {
            API.getInstance().log(LogLevel.ERROR, "Schematic \"" + schematic + "\" failed to load", e);
        } catch (NullPointerException e) {
            API.getInstance().log(LogLevel.WARN, "Player \"" + player.getName() +
                    "\" has left the server while painting.", e);
        }
    }

    private AffineTransform getRotation() {
        return new AffineTransform()
                .rotateX(((int) API.getDouble("Brushes." + name + ".Rotate_X") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_X"))
                .rotateY(((int) API.getDouble("Brushes." + name + ".Rotate_Y") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_Y"))
                .rotateZ(((int) API.getDouble("Brushes." + name + ".Rotate_Z") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_Z"));
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(API.getString("Brushes." + name + ".Material"))));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(API.color("&aSchematicBrushes~" + name));
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
}