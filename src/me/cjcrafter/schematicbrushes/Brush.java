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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

class Brush {

    private static final Random rand = new Random();

    private String name;
    private List<Clipboard> schematics;

    Brush(String name) {
        this.name = name.split("\\.")[1];
        this.schematics = new ArrayList<>();

        API.getList("Brushes." + this.name + ".Schematics").forEach(str -> schematics.add(API.getSchematic(str)));
    }

    void paste(Player player, Location loc) {
        Clipboard toPaste = schematics.get(rand.nextInt(schematics.size()));
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()), -1)) {
            ClipboardHolder holder = new ClipboardHolder(toPaste);
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
            System.err.println("There was an error pasting schematic \"" + toPaste.toString() + "\"");
        }
    }

    private AffineTransform getRotation() {
        AffineTransform rotation = new AffineTransform();
        rotation = rotation.rotateX(((int) API.getDouble("Brushes." + name + ".Rotate_X") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_X"));
        rotation = rotation.rotateY(((int) API.getDouble("Brushes." + name + ".Rotate_Y") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_Y"));
        rotation = rotation.rotateZ(((int) API.getDouble("Brushes." + name + ".Rotate_Z") == -1) ? rand.nextInt(4) * 90: API.getDouble("Brushes." + name + ".Rotate_Z"));
        return rotation;
    }

    ItemStack getItem() {
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(API.getString("Brushes." + name + ".Material"))));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("SchematicBrushes~" + name);
        item.setItemMeta(meta);
        return item;
    }
}