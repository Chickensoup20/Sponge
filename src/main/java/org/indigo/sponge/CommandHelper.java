package org.indigo.sponge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.functions.Item;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.rooms.Room;
import org.indigo.sponge.rooms.RoomType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.indigo.sponge.Sponge.*;

public class CommandHelper {
    public static LiteralArgumentBuilder<CommandSourceStack> flyspeedCommand() {
        return Commands.literal("flyspeed")
                .then(Commands.argument("speed", IntegerArgumentType.integer(1, 1000)).executes(CommandHelper::runFlySpeedLogic));

    }

    private static int runFlySpeedLogic(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        int speed = IntegerArgumentType.getInteger(ctx, "speed");
        player.setFlySpeed((float) speed / 1000);
        Utils.sendSystemMessage(player, "Succesfully set your flightspeed to " + speed + ".");
        return Command.SINGLE_SUCCESS;
    }

    public static LiteralCommandNode<CommandSourceStack> devCommand() {

        return Commands.literal("dev")
                .requires(sender -> sender.getSender().hasPermission("permission.dev"))
                .executes(ctx -> {
                    Player player = ctx.getSource().getPlayerOrThrow();
                    Sponge.playerStates.get(player).applyState(SpongePlayer.State.DEV);
                    Utils.sendSystemMessage(player, "You are now in dev mode.");
                    return Command.SINGLE_SUCCESS;
                }).build();

    }

    public static LiteralCommandNode<CommandSourceStack> lobbyCommand() {

        return Commands.literal("lobby")
                .executes(ctx -> {
                    Player player = ctx.getSource().getPlayerOrThrow();
                    Sponge.playerStates.get(player).applyState(SpongePlayer.State.LOBBY);
                    Utils.sendSystemMessage(player, "You are now in the lobby.");
                    return Command.SINGLE_SUCCESS;
                }).build();

    }

    public static LiteralCommandNode<CommandSourceStack> giveCommand() {

        return Commands.literal("giveitem")
                .requires(sender -> sender.getSender().hasPermission("permission.dev"))
                .then(Commands.argument("item", StringArgumentType.word())
                        .suggests(CommandHelper::getItemSuggestions)
                        .executes(ctx -> {
                            Player player = ctx.getSource().getPlayerOrThrow();
                            player.give(Sponge.itemDic.get(StringArgumentType.getString(ctx, "item")));
                            Utils.sendSystemMessage(player, "You have been given " + StringArgumentType.getString(ctx, "item"));
                            return Command.SINGLE_SUCCESS;
                        })).build();

    }

    private static CompletableFuture<Suggestions> getItemSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        // Suggest 1, 16, 32, and 64 to the user when they reach the 'amount' argument
        for (String itemID : Sponge.itemDic.keySet()) {
            builder.suggest(itemID);
        }
        return builder.buildFuture();
    }

    public static LiteralCommandNode<CommandSourceStack> rooms() {

        return Commands.literal("rooms")
            .requires(sender -> sender.getSender().hasPermission("permission.dev"))
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {

                        Room room = new Room(StringArgumentType.getString(ctx,"name"));
                        room.tpToWorld(ctx.getSource().getPlayerOrThrow());
                        playerStates.get(ctx.getSource().getPlayerOrThrow()).setBuilding(room);
                        return Command.SINGLE_SUCCESS;
                    }))
                )
                .then(Commands.literal("goto")
                        .then(Commands.argument("Room Name", StringArgumentType.word())
                                .suggests(((context, builder) -> {
                                    for (String room : rooms.keySet())
                                        builder.suggest(room);
                                    return builder.buildFuture();
                                }))
                                .executes(ctx -> {
                                    Room room = rooms.get(StringArgumentType.getString(ctx, "Room Name"));
                                    room.tpToWorld(ctx.getSource().getPlayerOrThrow());
                                    playerStates.get(ctx.getSource().getPlayerOrThrow()).setBuilding(room);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests(((context, builder) -> {
                                for (String room : rooms.keySet())
                                    builder.suggest(room);
                                return builder.buildFuture();
                            }))
                            .executes(ctx -> {

                                try {
                                    rooms.get(StringArgumentType.getString(ctx,"name")).updateBounds();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                return Command.SINGLE_SUCCESS;
                        }))
                )
                .then(Commands.literal("tools")
                        .executes(ctx -> {

                            ctx.getSource().getPlayerOrThrow().give(entranceWand,exitWand);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("info")
                        .then(Commands.argument("room", StringArgumentType.word())
                                .suggests(((context, builder) -> {
                                    for (String room : rooms.keySet())
                                        builder.suggest(room);
                                    return builder.buildFuture();
                                }))
                                .executes(ctx -> {
                                    Room room = rooms.get(StringArgumentType.getString(ctx,"room"));
                                    Player player = ctx.getSource().getPlayerOrThrow();
                                    player.sendMessage(room.getName() + "info:");
                                    player.sendMessage("Floor: " + room.getFloor());
                                    player.sendMessage("Room Type: " + room.getRoomType());
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.literal("name")
                                    .then(Commands.argument("name", StringArgumentType.word())
                                            .executes(ctx -> {
                                                rooms.get(StringArgumentType.getString(ctx,"room")).setName(StringArgumentType.getString(ctx,"name"));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                                )
                                .then(Commands.literal("floor")
                                        .then(Commands.argument("floor", StringArgumentType.word())
                                        .executes(ctx -> {
                                            rooms.get(StringArgumentType.getString(ctx,"room")).setFloor(StringArgumentType.getString(ctx,"floor"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                ))
                                .then(Commands.literal("type")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                            .suggests(((context, builder) -> {
                                                for (RoomType type : RoomType.values())
                                                    builder.suggest(type.toString());
                                                return builder.buildFuture();
                                            }))
                                        .executes(ctx -> {
                                            rooms.get(StringArgumentType.getString(ctx,"room")).setRoomType(RoomType.valueOf(StringArgumentType.getString(ctx,"type")));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                ))

                        )
                )


                .build();

    }

    private static CompletableFuture<Suggestions> getRoomSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        builder.suggest("create");
        return builder.buildFuture();
    }
}
