package org.indigo.sponge.game;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.game.rooms.RoomInstance;

import java.util.*;

import static org.indigo.sponge.Sponge.*;

public class Game {
    private List<Player> players = new ArrayList<>();
    private UUID uuid;
    private SlimeWorld slimeWorld;
    private World world;
    private List<BoundingBox> roomBoxes = new ArrayList<>();
    public List<RoomInstance> instances = new ArrayList<>();
    public List<Player> alivePlayers;
    public Floor floor;
    private int roomIndex = 0;
    private Branch currentBranch = null;
    public HashMap<RoomTemplate.RoomType, List<RoomTemplate>> rooms = new HashMap<>();
    public boolean newBranch = false;
    public boolean canBranch = false;

    /**
     * Starts a new game instance for the given players: creates a dedicated
     * SlimeWorld and pastes the starting "test" room at the origin.
     * @param players the players participating in this game
     */
    public Game(List<Player> players, Floor floor){
        this.players = players;
        this.uuid = UUID.randomUUID();
        slimeWorld = asp.createEmptyWorld(uuid.toString(), false, new SlimePropertyMap(), loader);

        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        RoomInstance room = new RoomInstance(allRooms.get("intro"));
        Location firstPaste = new Location(world, 0, 51, 0);
        room.generate(firstPaste, 0);
        roomBoxes.add(room.boundingBox);
        instances.add(room);
        runningGames.add(this);
        alivePlayers = players;
        this.floor = floor;
        this.rooms = floor.rooms;
    }

    /**
     * Teleports all players in this game to the starting room.
     */
    public void start() {
        for (Player player : players)
            player.teleport(new Location(world, 0, 51, 0));
    }

    /**
     * Attempts to generate a new room from the given exit by searching all known
     * room templates for one that fits without overlapping existing rooms.
     * @param exit the exit connector to expand from
     */
    public void expandFrom(Connector exit) {
        if (exit.isBlocked()) return;

        RoomInstance sourceRoom = findOwningInstance(exit);
        RoomNode requiredRoomType = floor.floorMap.get(roomIndex);
        List<RoomTemplate> pool = new ArrayList<>();
        canBranch = false;
        Bukkit.broadcast(Component.text(requiredRoomType.toString()));
        if(currentBranch != null){
            if(currentBranch.getChildren().get(roomIndex) instanceof Branch){
                canBranch = true;
            }
        } else {
            if(requiredRoomType instanceof Branch){
                canBranch = true;
            }
        }

        if(canBranch){
            Bukkit.broadcast(Component.text("BRANCH!!!"));
            Bukkit.broadcast(Component.text(branchRooms.getFirst().name));
            pool = branchRooms;
        } else {
            pool = rooms.get(requiredRoomType);
            roomIndex++;
        }

        Collections.shuffle(pool);
        RoomInstance next = RoomInstance.generateFromExit(exit, world, pool, instances, sourceRoom);
        if (next == null) return;

        roomBoxes.add(next.boundingBox);
        instances.add(next);

        if (sourceRoom != null) {
            for (Connector otherExit : sourceRoom.exits) {
                if (otherExit != exit && !otherExit.isBlocked()) {
                    otherExit.block(world);
                }
            }
        }

        for (Connector nextExit : next.exits) {
            if (!RoomInstance.canGenerateFrom(nextExit, pool, instances, next)) {
                nextExit.block(world);
            }
        }
    }

    /**
     * Finds the room instance that owns the given connector (as an exit or entrance).
     * @param exit the connector to locate
     * @return the owning room instance, or null if not found
     */
    private RoomInstance findOwningInstance(Connector exit) {
        for (RoomInstance instance : instances) {
            if (instance.exits.contains(exit) || instance.entrance == exit) {
                return instance;
            }
        }
        return null;
    }
}