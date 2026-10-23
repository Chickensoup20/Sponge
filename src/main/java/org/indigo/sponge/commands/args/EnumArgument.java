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

import java.util.concurrent.CompletableFuture;

/**
 * Parses any enum by constant name (case-insensitive), suggesting every constant.
 */
public final class EnumArgument<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            value -> MessageComponentSerializer.message().serialize(Component.text("Unknown value: " + value)));

    private final Class<E> type;

    public EnumArgument(Class<E> type) {
        this.type = type;
    }

    @Override
    public E convert(String value) throws CommandSyntaxException {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw UNKNOWN.create(value);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        for (E constant : type.getEnumConstants()) {
            if (constant.name().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(constant.name());
            }
        }
        return builder.buildFuture();
    }
}
