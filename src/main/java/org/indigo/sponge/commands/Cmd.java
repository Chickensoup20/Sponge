package org.indigo.sponge.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Shared helpers so individual commands only contain their own logic.
 */
public final class Cmd {

    @FunctionalInterface
    public interface PlayerBody {
        void run(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException;
    }

    /**
     * Wraps a command body that needs a player; non-players get a normal
     * "requires a player" error instead of a crash.
     */
    public static Command<CommandSourceStack> player(PlayerBody body) {
        return ctx -> {
            body.run(ctx, ctx.getSource().getPlayerOrThrow());
            return Command.SINGLE_SUCCESS;
        };
    }

    public static Predicate<CommandSourceStack> dev() {
        return source -> source.getSender().hasPermission(Perms.DEV);
    }

    private Cmd() {
    }
}
