package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.registries.Players;

public final class DevCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("dev")
                .requires(Cmd.dev())
                .executes(Cmd.player(this::run))
                .build();
    }

    @Override
    public String description() {
        return "Switch to dev mode";
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        Players.get(player).applyState(SpongePlayer.State.DEV);
        Utils.sendSystemMessage(player, "You are now in dev mode.");
    }
}
