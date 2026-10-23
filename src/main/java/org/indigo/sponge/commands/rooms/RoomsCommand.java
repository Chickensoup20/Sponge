package org.indigo.sponge.commands.rooms;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.commands.args.EnumArgument;
import org.indigo.sponge.commands.args.RoomArgument;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.registries.Items;
import org.indigo.sponge.registries.Players;
import org.indigo.sponge.registries.RoomRegistry;

public final class RoomsCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("rooms")
                .requires(Cmd.dev())
                .executes(Cmd.player(this::list))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("type", new EnumArgument<>(RoomTemplate.RoomType.class))
                                        .executes(Cmd.player(this::create)))))
                .then(Commands.literal("goto")
                        .then(Commands.argument("room", new RoomArgument())
                                .executes(Cmd.player(this::goTo))))
                .then(Commands.literal("save")
                        .then(Commands.argument("room", new RoomArgument())
                                .executes(Cmd.player(this::save))))
                .then(Commands.literal("tools")
                        .executes(Cmd.player(this::tools)))
                .build();
    }

    @Override
    public String description() {
        return "Create, edit and save room templates";
    }

    private void list(CommandContext<CommandSourceStack> ctx, Player player) {
        for (RoomTemplate room : RoomRegistry.all()) {
            player.sendMessage("Room name: " + room.name + ", " + room.roomType);
        }
    }

    private void create(CommandContext<CommandSourceStack> ctx, Player player) {
        String name = StringArgumentType.getString(ctx, "name");
        RoomTemplate.RoomType type = ctx.getArgument("type", RoomTemplate.RoomType.class);
        RoomTemplate room = new RoomTemplate(name, "sponge", type);
        room.tpToWorld(player);
        Players.get(player).setBuilding(room);
    }

    private void goTo(CommandContext<CommandSourceStack> ctx, Player player) {
        RoomTemplate room = ctx.getArgument("room", RoomTemplate.class);
        room.tpToWorld(player);
        Players.get(player).setBuilding(room);
    }

    private void save(CommandContext<CommandSourceStack> ctx, Player player) {
        RoomTemplate room = ctx.getArgument("room", RoomTemplate.class);
        room.saveSchematicAsync().whenComplete((v, err) -> {
            if (err != null) Utils.sendSystemMessage(player, "<red>Save failed: " + err.getMessage());
            else Utils.sendSystemMessage(player, "Saved " + room.name);
        });
    }

    private void tools(CommandContext<CommandSourceStack> ctx, Player player) {
        player.give(Items.entranceWand, Items.exitWand);
    }
}
