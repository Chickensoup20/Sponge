package org.indigo.sponge.game;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.block.CustomBlock;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.game.rooms.RoomInstance;
import org.indigo.sponge.registries.CustomBlocks;
import org.indigo.sponge.registries.Games;
import org.indigo.sponge.registries.Players;
import org.indigo.sponge.registries.RoomRegistry;

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
        this.floor = floor;
        this.rooms = floor.rooms;
        slimeWorld = asp.createEmptyWorld(uuid.toString(), false, new SlimePropertyMap(), loader);

        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        RoomInstance room = new RoomInstance(rooms.get(RoomTemplate.RoomType.INTRO).getFirst());
        Location firstPaste = new Location(world, 0, 51, 0);
        room.generate(firstPaste, 0);
        roomBoxes.add(room.boundingBox);
        instances.add(room);
        Games.runningGames.add(this);
        alivePlayers = players;

        currentBranch = new Branch(floor.floorMap);
        world.setGameRule(GameRules.ADVANCE_TIME,false);
    }

    /**
     * Teleports all players in this game to the starting room.
     */
    public void start() {
        for (Player player : players) {
            Players.get(player).applyState(SpongePlayer.State.INGAME);
        }
        teleportPlayersToSpawningPoints(instances.getFirst());
    }

    public void teleportPlayersToSpawningPoints(RoomInstance next){
        List<Location> spawnLocs = new ArrayList<>();
        for(int x = (int) next.boundingBox.getMinX(); x < next.boundingBox.getMaxX(); x++){
            for(int y = (int) next.boundingBox.getMinY(); y < next.boundingBox.getMaxY(); y++){
                for(int z = (int) next.boundingBox.getMinZ(); z < next.boundingBox.getMaxZ(); z++){
                    Location location = new Location(world,x,y,z);
                    if(CustomBlock.getInstance(location.getBlock()) == CustomBlocks.spawnLocationIndicator){
                        location.getBlock().setType(Material.AIR);
                        spawnLocs.add(location);
                    }
                }
            }
        }
        if(!spawnLocs.isEmpty()){
            int index = 0;
            for(Player player : players){
                player.teleport(spawnLocs.get(index));
                index++;
                if(index >= spawnLocs.size()){
                    index = 0;
                }
            }
        }
    }

    /**
     * Attempts to generate a new room from the given exit by searching all known
     * room templates for one that fits without overlapping existing rooms.
     * @param exit the exit connector to expand from
     */
    public void expandFrom(Connector exit) {
        if (exit.isBlocked()) return;

        if(roomIndex >= floor.floorMap.size()){
            for(Player player : players){
                Players.get(player).applyState(SpongePlayer.State.LOBBY);
            }
        }
        RoomInstance sourceRoom = findOwningInstance(exit);
        RoomNode requiredRoomType = currentBranch.getChildren().get(roomIndex);
        List<RoomTemplate> pool = new ArrayList<>();
        if(canBranch){
            //Currently in a branch making a decision
            if(newBranch){
                currentBranch = (Branch) currentBranch.getChildren().get(roomIndex);
                roomIndex = 0;
            } else {
                roomIndex++;

            }
            newBranch = false;
            canBranch = false;
            pool = rooms.get(currentBranch.getChildren().get(roomIndex));

        } else {
            Bukkit.broadcast(Component.text(requiredRoomType.toString()));
            if(currentBranch.getChildren().get(roomIndex) instanceof Branch){
                //Current room is a branch
                canBranch = true;
                Bukkit.broadcast(Component.text("BRANCH!!!"));
                List<RoomTemplate> branchPool = new ArrayList<>(RoomRegistry.all(RoomTemplate.RoomType.BRANCH));
                Bukkit.broadcast(Component.text(branchPool.getFirst().name));
                pool = branchPool;
            } else {
                //Current room is not a branch
                pool = rooms.get(requiredRoomType);
                roomIndex++;
            }

        }//7739

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

        teleportPlayersToSpawningPoints(next);
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