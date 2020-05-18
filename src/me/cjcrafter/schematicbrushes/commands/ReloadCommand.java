package me.cjcrafter.schematicbrushes.commands;

import me.cjcrafter.core.commands.SubCommand;
import me.cjcrafter.core.utils.MessageUtils;
import me.cjcrafter.schematicbrushes.SchematicBrushes;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(String parentPrefix) {
        super(parentPrefix, "reload", "Reloads the plugin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SchematicBrushes.getInstance().onReload();
        MessageUtils.message(sender, "&aReloaded SchematicBrushes");
    }
}
