package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.commands.args.ItemArgument;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.registries.Items;

import java.util.List;

public final class GiveItemCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("giveitem")
                .requires(Cmd.dev())
                .then(Commands.argument("item", new ItemArgument())
                        .executes(Cmd.player(this::run)))
                .build();
    }

    @Override
    public String description() {
        return "Give yourself a Sponge item";
    }

    @Override
    public List<String> aliases() {
        return List.of("get");
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        String id = ctx.getArgument("item", String.class);
        player.give(Items.itemDic.get(id));
        Utils.sendSystemMessage(player, "You have been given " + id);
    }
}
