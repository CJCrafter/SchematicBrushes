package me.cjcrafter.schematicbrushes.listeners;

import me.cjcrafter.core.file.Configuration;
import me.cjcrafter.core.utils.DebugUtils;
import me.cjcrafter.core.utils.Log;
import me.cjcrafter.schematicbrushes.Brush;
import me.cjcrafter.schematicbrushes.SchematicBrushes;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractListener implements Listener {

    private static Configuration config = SchematicBrushes.getConfiguration();

    /**
     * For some dumb reason (bad coding), this gets
     * cancelled when clicking in air. Just fixing that
     * behavior here
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void interactFixer(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR) {
            e.setCancelled(false);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("schematicbrushes.use")) {
            return;
        }

        ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
        if (meta == null) return;
        if (!meta.hasDisplayName()) return;
        String[] split = ChatColor.stripColor(meta.getDisplayName()).split("~");
        if (split.length != 2) return;

        Brush brush = Brush.forName(split[1]);
        if (brush == null) {
            DebugUtils.log(Log.WARN, "Missing brush " + split[1] + ". You can probably ignore this error.");
            return;
        }
        Block target = e.getPlayer().getTargetBlock(null, config.getInt("Max_Brush_Distance"));
        brush.click(e.getPlayer(), target.getLocation());
    }
}
