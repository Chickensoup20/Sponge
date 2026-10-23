package org.indigo.sponge.commands.dev;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.indigo.sponge.commands.Cmd;
import org.indigo.sponge.commands.SpongeCommand;
import org.indigo.sponge.functions.Utils;

import java.util.List;

public final class FlySpeedCommand implements SpongeCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("flyspeed")
                .then(Commands.argument("speed", IntegerArgumentType.integer(1, 1000))
                        .executes(Cmd.player(this::run)))
                .build();
    }

    @Override
    public String description() {
        return "Set your flight speed (1-1000)";
    }

    @Override
    public List<String> aliases() {
        return List.of("fs", "flightspeed");
    }

    private void run(CommandContext<CommandSourceStack> ctx, Player player) {
        int speed = IntegerArgumentType.getInteger(ctx, "speed");
        player.setFlySpeed((float) speed / 1000);
        Utils.sendSystemMessage(player, "Successfully set your flightspeed to " + speed + ".");
    }
}
