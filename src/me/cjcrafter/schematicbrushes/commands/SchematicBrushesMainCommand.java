package me.cjcrafter.schematicbrushes.commands;

import me.cjcrafter.core.commands.MainCommand;

import java.util.Arrays;

public class SchematicBrushesMainCommand extends MainCommand {

    public SchematicBrushesMainCommand() {
        super("schematicbrushes", "schematicbrushes.command");

        setAliases(Arrays.asList("sb", "schembrush"));
        registerHelp();

        commands.register(new ReloadCommand("sb"));
        commands.register(new GetCommand("sb"));
    }
}
