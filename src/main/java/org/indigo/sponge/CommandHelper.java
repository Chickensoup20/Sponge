package org.indigo.sponge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static org.indigo.sponge.Sponge.mm;

public class CommandHelper {
    public static LiteralArgumentBuilder<CommandSourceStack> flyspeedCommand() {
        return Commands.literal("flyspeed")
            .then(Commands.argument("speed", IntegerArgumentType.integer(1, 1000)).executes(CommandHelper::runFlySpeedLogic));

    }

    private static int runFlySpeedLogic(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        int speed = IntegerArgumentType.getInteger(ctx, "speed");
        player.setFlySpeed((float) speed /1000);
        player.sendMessage(mm.deserialize(Colors.spongeLogo + Colors.toMM(Colors.GOLD_LIGHT) + " Succesfully set your flightspeed to " + speed));
        return Command.SINGLE_SUCCESS;
    }

    public static LiteralCommandNode<CommandSourceStack> devCommand() {

        return Commands.literal("dev")
                .executes(ctx -> {
                    Player player = ctx.getSource().getPlayerOrThrow();
                    Sponge.playerStates.get(player).applyState(SpongePlayer.State.DEV);
                    return Command.SINGLE_SUCCESS;
                }).build();

    }
    public static LiteralCommandNode<CommandSourceStack> lobbyCommand() {

        return Commands.literal("lobby")
                .executes(ctx -> {
                    Player player = ctx.getSource().getPlayerOrThrow();
                    Sponge.playerStates.get(player).applyState(SpongePlayer.State.LOBBY);
                    return Command.SINGLE_SUCCESS;
                }).build();

    }
}
