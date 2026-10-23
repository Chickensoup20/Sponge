package org.indigo.sponge.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

/**
 * A top-level command. Add new ones to {@link SpongeCommands#ALL}.
 */
public interface SpongeCommand {
    LiteralCommandNode<CommandSourceStack> build();

    default String description() {
        return null;
    }

    default List<String> aliases() {
        return List.of();
    }
}
