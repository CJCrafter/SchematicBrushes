package me.cjcrafter.core.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    /**
     * Don't let anyone instantiate this class
     */
    private MessageUtils() {
    }

    /**
     * Sends a colored message to the given player
     *
     * @param user Player to send message to
     * @param msg The message to color and send
     */
    public static void message(CommandSender user, String msg) {
        user.sendMessage(StringUtils.color(msg));
    }

    public static void actionbar(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.color(msg)));
    }

    public static void title(Player player, String msg) {
    }
}
