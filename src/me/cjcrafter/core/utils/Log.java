package me.cjcrafter.core.utils;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * This enum is for the different levels that can be
 * used to log information in the debugger. This is
 * allows user to filter out information that may
 * not be important while still receiving important info
 *
 * @see DebugUtils
 */
public enum Log {

    DEBUG(3, Level.INFO),
    WARN(2, Level.WARNING),
    ERROR(1, Level.SEVERE),
    INFO(1, Level.INFO);

    private final int bound;
    private final Level level;

    /**
     * @param bound The integer level to define how fine a task is
     * @param level The Logger compatible level to use for logging
     */
    Log(int bound, @Nonnull Level level) {
        this.bound = bound;
        this.level = level;
    }

    public int getBound() {
        return bound;
    }

    @Nonnull
    public Level getLevel() {
        return level;
    }
}
