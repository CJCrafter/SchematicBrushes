package me.cjcrafter.schematicbrushes.util;

public enum LogLevel {

    DEBUG(3),
    WARN(2),
    ERROR(1);

    int level;

    LogLevel(int level) {
        this.level = level;
    }

    public boolean isValidLevel(int level) {
        return this.level <= level;
    }
}
