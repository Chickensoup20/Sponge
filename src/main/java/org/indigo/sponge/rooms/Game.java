package org.indigo.sponge.rooms;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.indigo.sponge.Sponge.*;

public class Game {
    private List<Player> players = new ArrayList<>();
    private UUID uuid;
    private SlimeWorld slimeWorld;
    private World world;

    public Game(List<Player> players){
        this.players = players;
        this.uuid = UUID.randomUUID();
        slimeWorld = asp.createEmptyWorld(uuid.toString(), false, new SlimePropertyMap(), loader);

        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        RoomInstance room = new RoomInstance(allRooms.get("test"));
        Location firstPaste = new Location(world, 0, 51, 0);
        room.generate(firstPaste, 0);
    }

    public void start() {
        for (Player player : players)
            player.teleport(new Location(world, 0, 51, 0));
    }

}
