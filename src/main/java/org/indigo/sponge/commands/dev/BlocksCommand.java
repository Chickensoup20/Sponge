package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.menus.BlocksMenu;

public final class BlocksCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("blocks")
                .executes(Cmd.player(this::run))
                .build();
    }

    @Override
    public String description() {
        return "Open the custom blocks menu";
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        player.openInventory(new BlocksMenu(Sponge.plugin).getInventory());
    }
}
