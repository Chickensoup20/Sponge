package org.indigo.sponge.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.registries.Players;

public final class LobbyCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("lobby")
                .executes(Cmd.player(this::run))
                .build();
    }

    @Override
    public String description() {
        return "Return to the lobby";
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        Players.get(player).applyState(SpongePlayer.State.LOBBY);
        Utils.sendSystemMessage(player, "You are now in the lobby.");
    }
}
