package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.indigo.sponge.rooms.Room;

import java.util.HashMap;

public class SpongePlayer {
    private final Player player;
    private State currentState;
    private Room buildingRoom;
    public HashMap<String, Object> tempVars = new HashMap<>();

    public enum State {
        LOBBY,
        INGAME,
        DEV,
        BUILD
    }

    public SpongePlayer(Player player, State state) {
        currentState = state;
        this.player = player;
        buildingRoom = null;
        Sponge.playerStates.put(player,this);
    }



    public void applyState() {
        Location teleportLoc;
        switch (currentState){
            case DEV -> {
                player.setGameMode(GameMode.CREATIVE);
                break;
            }
            case LOBBY -> {
                player.setGameMode(GameMode.ADVENTURE);
                buildingRoom = null;
                break;
            }
            case BUILD -> {
                player.setGameMode(GameMode.CREATIVE);
                return;
            }

        }
        player.getInventory().clear();
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setSaturation(20);
        player.setFoodLevel(20);
        player.setFlySpeed(0.1f);
        World lobby = Bukkit.getWorld("lobby");
        player.teleport(lobby.getSpawnLocation());

    }


    public void applyState(State state) {
        setCurrentState(state);
        applyState();
    }

    private void setCurrentState(State state) {
        currentState = state;
    }

    public State getState(){
        return currentState;
    }

    public void setBuilding(Room room){
        buildingRoom = room;
        applyState(State.BUILD);

    }

    public Room getBuildingRoom() {
        return buildingRoom;
    }
}
