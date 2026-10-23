package org.indigo.sponge.commands.args;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.registries.RoomRegistry;

import java.util.concurrent.CompletableFuture;

/**
 * Parses a room name straight into its {@link RoomTemplate}, suggesting every registered room.
 */
public final class RoomArgument implements CustomArgumentType.Converted<RoomTemplate, String> {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            name -> MessageComponentSerializer.message().serialize(Component.text("Unknown room: " + name)));

    @Override
    public RoomTemplate convert(String name) throws CommandSyntaxException {
        try {
            return RoomRegistry.get(name);
        } catch (IllegalArgumentException e) {
            throw UNKNOWN.create(name);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        for (RoomTemplate room : RoomRegistry.all()) {
            if (room.name.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(room.name);
            }
        }
        return builder.buildFuture();
    }
}
