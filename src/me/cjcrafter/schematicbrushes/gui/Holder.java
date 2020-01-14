package me.cjcrafter.schematicbrushes.gui;

import me.cjcrafter.schematicbrushes.API;
import me.cjcrafter.schematicbrushes.util.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class Holder implements InventoryHolder {
    
    private Inventory inv;
    
    public Holder() {
        this.inv = Bukkit.createInventory(this, InventoryType.CHEST, API.color("&2Schematic Brushes"));
        API.log(LogLevel.DEBUG, "Created Inventory");
    }
    
    public void add(ItemStack...items) {
        inv.addItem(items);
    }
    
    public void display(Player player) {
        player.openInventory(inv);
        API.log(LogLevel.DEBUG, "Displaying inventory with " + inv.getContents().length + " items.");
    }
    
    @Override
    @Nonnull
    public Inventory getInventory() {
        return inv;
    }
}
