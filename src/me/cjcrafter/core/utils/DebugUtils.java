package me.cjcrafter.core.utils;

import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public class DebugUtils {

    public static Logger logger;
    public static int level;

    /**
     * Don't let anyone instantiate this class
     */
    private DebugUtils() {
    }

    /**
     * Logs multiple messages at the given level. This should be
     * used for reporting user error, debugging, warnings, and
     * general information.
     *
     * @param level Level to log at
     * @param msg Message to log
     */
    public static void log(@Nonnull Log level, @Nonnull String...msg) {
        if (level.getBound() > DebugUtils.level) return;
        for (String str: msg) {
            logger.log(level.getLevel(), str);
        }
    }

    /**
     * Logs an error with the given message. This shouldn't be
     * used for user error, this should be used for "unhandled"
     * exceptions
     *
     * @param level Level to log at
     * @param msg Message to log
     * @param error Error to log
     */
    public static void log(@Nonnull Log level, String msg, @Nullable Throwable error) {
        if (level.getBound() > DebugUtils.level) return;
        logger.log(level.getLevel(), msg, error);
    }
}
