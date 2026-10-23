package org.indigo.sponge.commands;

import io.papermc.paper.command.brigadier.Commands;
import org.indigo.sponge.commands.dev.BlocksCommand;
import org.indigo.sponge.commands.dev.DevCommand;
import org.indigo.sponge.commands.dev.FlySpeedCommand;
import org.indigo.sponge.commands.dev.GiveItemCommand;
import org.indigo.sponge.commands.dev.TestCommand;
import org.indigo.sponge.commands.rooms.RoomsCommand;

import java.util.List;

/**
 * The one list of every command the plugin registers.
 */
public final class SpongeCommands {
    private static final List<SpongeCommand> ALL = List.of(
            new LobbyCommand(),
            new DevCommand(),
            new FlySpeedCommand(),
            new GiveItemCommand(),
            new BlocksCommand(),
            new TestCommand(),
            new RoomsCommand()
    );

    public static void register(Commands registrar) {
        for (SpongeCommand command : ALL) {
            registrar.register(command.build(), command.description(), command.aliases());
        }
    }

    private SpongeCommands() {
    }
}
