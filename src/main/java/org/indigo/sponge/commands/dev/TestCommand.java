package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.game.Game;
import org.indigo.sponge.registries.FloorRegistry;

import java.util.List;

public final class TestCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("test")
                .executes(Cmd.player(this::run))
                .build();
    }

    @Override
    public String description() {
        return "Start a solo test game on the sponge floor";
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        new Game(List.of(player), FloorRegistry.get("sponge")).start();
    }
}
