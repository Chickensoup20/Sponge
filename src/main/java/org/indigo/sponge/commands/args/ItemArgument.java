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
import org.indigo.sponge.registries.Items;

import java.util.concurrent.CompletableFuture;

/**
 * Parses a Sponge item id, checking it exists in {@link Items#itemDic}. Yields the id.
 */
public final class ItemArgument implements CustomArgumentType.Converted<String, String> {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            id -> MessageComponentSerializer.message().serialize(Component.text("Unknown item: " + id)));

    @Override
    public String convert(String id) throws CommandSyntaxException {
        if (!Items.itemDic.containsKey(id)) {
            throw UNKNOWN.create(id);
        }
        return id;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        for (String id : Items.itemDic.keySet()) {
            if (id.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(id);
            }
        }
        return builder.buildFuture();
    }
}
