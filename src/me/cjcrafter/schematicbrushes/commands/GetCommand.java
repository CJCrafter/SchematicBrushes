package me.cjcrafter.schematicbrushes.commands;

import me.cjcrafter.core.commands.SubCommand;
import me.cjcrafter.core.utils.MessageUtils;
import me.cjcrafter.schematicbrushes.Brush;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GetCommand extends SubCommand {

    public GetCommand(String parentPrefix) {
        super(parentPrefix, "get", "Gets a given brush", "<brush>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.message(sender, "&cPlayer only command!");
            return;
        } else if (args.length < 1) {
            sendHelp(sender, args);
            return;
        }

        Player player = (Player) sender;
        Brush brush = Brush.forName(args[0]);
        if (brush == null) {
            MessageUtils.message(player, "&cInvalid brush name!");
            return;
        }

        player.getInventory().addItem(brush.getItem());
    }

    @Override
    public List<String> handleCustomTag(String[] args, String tag) {
        switch (tag) {
            case "<brush>":
                return new ArrayList<>(Brush.brushes.keySet());
            default:
                return new ArrayList<>();
        }
    }
}
