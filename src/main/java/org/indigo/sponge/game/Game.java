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
    private List<SpongePlayer> players = new ArrayList<>();
    private UUID uuid;
    private SlimeWorld slimeWorld;
    private World world;
    private List<BoundingBox> roomBoxes = new ArrayList<>();
    public List<RoomInstance> instances = new ArrayList<>();
    private List<SpongePlayer> alivePlayers = new ArrayList<>();
    public Floor floor;
    private int roomIndex = 0;
    private Branch currentBranch = null;
    public HashMap<RoomTemplate.RoomType, List<RoomTemplate>> rooms = new HashMap<>();
    public boolean newBranch = false;
    public boolean canBranch = false;

    public Game(List<SpongePlayer> players, Floor floor){
        for(SpongePlayer player: players){
            if (player.isInGame()){
                throw new IllegalStateException(player.getPlayer().getName() + " is in a game");
            }
        }
        this.players.addAll(players);

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
        alivePlayers.addAll(players);

        currentBranch = new Branch(floor.floorMap);
        world.setGameRule(GameRules.ADVANCE_TIME,false);
    }

    public void start() {
        for (SpongePlayer player : players) {
            player.applyState(SpongePlayer.State.INGAME);
            player.setGame(this);
        }
        teleportPlayersToSpawningPoints(instances.getFirst());
    }

    public void end(){

        for(SpongePlayer player : players){
            player.applyState(SpongePlayer.State.LOBBY);
            player.setGame(null);
        }
        Games.runningGames.remove(this);
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
            for(SpongePlayer player : players){
                player.getPlayer().teleport(spawnLocs.get(index));
                index++;
                if(index >= spawnLocs.size()){
                    index = 0;
                }
            }
        }
    }

    public void expandFrom(Connector exit) {
        if (exit.isBlocked()) return;

        if(roomIndex >= currentBranch.getChildren().size()){
            for(SpongePlayer player : players){
                player.applyState(SpongePlayer.State.LOBBY);
            }
            return;
        }
        RoomInstance sourceRoom = findOwningInstance(exit);
        RoomNode requiredRoomType = currentBranch.getChildren().get(roomIndex);

        List<RoomTemplate> pool = chooseNextRoomPool(requiredRoomType);

        RoomInstance next = RoomInstance.generateFromExit(exit, world, pool, instances, sourceRoom);
        if (next == null) return;

        roomBoxes.add(next.boundingBox);
        instances.add(next);

        blockOffExits(sourceRoom,next,exit,pool);

        teleportPlayersToSpawningPoints(next);
    }

    private void blockOffExits(RoomInstance sourceRoom, RoomInstance nextRoom, Connector exit, List<RoomTemplate> pool){
        if (sourceRoom != null) {
            for (Connector otherExit : sourceRoom.exits) {
                if (otherExit != exit && !otherExit.isBlocked()) {
                    otherExit.block(world);
                }
            }
        }

        for (Connector nextExit : nextRoom.exits) {
            if (!RoomInstance.canGenerateFrom(nextExit, pool, instances, nextRoom)) {
                nextExit.block(world);
            }
        }
    }

    private List<RoomTemplate> chooseNextRoomPool(RoomNode requiredRoomType){
        List<RoomTemplate> pool = new ArrayList<>();
        Bukkit.broadcast(Component.text("Room index: " + roomIndex));
        newBranch = true;
        if(canBranch){
            //Currently in a branch making a decision
            Bukkit.broadcast(Component.text("Currently in a branch making a decision"));
            if(newBranch){
                currentBranch = (Branch) currentBranch.getChildren().get(roomIndex);
                roomIndex = 0;
                Bukkit.broadcast(Component.text("Decision made: Entered a new branch"));
            } else {
                roomIndex++;
                Bukkit.broadcast(Component.text("Decision made: Continue on current branch"));
            }
            Bukkit.broadcast(Component.text("Generating new room: " + currentBranch.getChildren().get(roomIndex)));
            newBranch = false;
            canBranch = false;
            pool = rooms.get(currentBranch.getChildren().get(roomIndex));

        } else {
            Bukkit.broadcast(Component.text("Continuing on current branch"));
            Bukkit.broadcast(Component.text("Generating new room: " + currentBranch.getChildren().get(roomIndex)));
            if(currentBranch.getChildren().get(roomIndex) instanceof Branch){
                //Current room is a branch
                canBranch = true;
                pool = new ArrayList<>(RoomRegistry.all(RoomTemplate.RoomType.BRANCH));
            } else {
                //Current room is not a branch
                pool = rooms.get(requiredRoomType);
                roomIndex++;
            }


        }
        Collections.shuffle(pool);
        return pool;
    }

    private RoomInstance findOwningInstance(Connector exit) {
        for (RoomInstance instance : instances) {
            if (instance.exits.contains(exit) || instance.entrance == exit) {
                return instance;
            }
        }
        return null;
    }

    public void removeAlivePlayer(SpongePlayer player){
        alivePlayers.remove(player);
    }

    public int amtAlivePlayers(){
        return alivePlayers.size();
    }

    public List<SpongePlayer> getAlivePlayers(){
        return alivePlayers;
    }
}