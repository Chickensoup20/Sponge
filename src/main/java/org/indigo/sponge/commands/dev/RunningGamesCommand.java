package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.game.Game;
import org.indigo.sponge.registries.Games;

import java.util.List;

public class RunningGamesCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("runninggames").executes(Cmd.player(this::run)).build();
    }

    @Override
    public String description() {
        return SpongeCommand.super.description();
    }

    @Override
    public List<String> aliases() {
        return SpongeCommand.super.aliases();
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player){
        for(Game game : Games.runningGames){
            player.sendMessage(game.floor.getFloorName() + ": " + game.amtAlivePlayers() + " alive players");
        }
    }
}
