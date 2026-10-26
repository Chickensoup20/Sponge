package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.indigo.sponge.game.Game;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.game.rooms.RoomAutosave;

import java.util.List;
import java.util.UUID;

public class SpongePlayer {
    private final UUID uuid;
    private Player player;
    private State currentState;
    private RoomTemplate buildingRoom;
    private boolean placingEntrance;
    private boolean placingExit;
    private Connector pendingExit;
    private Game game;

    public enum State {
        LOBBY,
        INGAME,
        DEV,
        BUILD
    }

    public SpongePlayer(Player player, State state) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.currentState = state;
        this.buildingRoom = null;
        this.game = null;
    }

    public void attach(Player player) {
        this.player = player;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public void applyState() {
        switch (currentState) {
            case DEV -> {
                player.setGameMode(GameMode.CREATIVE);
            }
            case LOBBY -> {
                player.setGameMode(GameMode.ADVENTURE);
                buildingRoom = null;
                clearBuildToolState();
                player.getInventory().clear();
                player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
                player.setSaturation(20);
                player.setFoodLevel(20);
                player.setFlySpeed(0.1f);
                World lobby = Bukkit.getWorld("lobby");
                player.teleport(lobby.getSpawnLocation());
            }
            case BUILD -> {
                player.setGameMode(GameMode.CREATIVE);
                return;
            }
            case INGAME -> {
                return;
            }
        }

    }

    public void applyState(State state) {
        if (currentState == State.BUILD && state != State.BUILD && buildingRoom != null) {
            RoomAutosave.saveOnLeave(buildingRoom, this);
        }
        setCurrentState(state);
        applyState();
    }

    private void setCurrentState(State state) {
        currentState = state;
    }

    public State getState() {
        return currentState;
    }

    public void setBuilding(RoomTemplate room) {
        if (currentState == State.BUILD && buildingRoom != null && buildingRoom != room) {
            RoomAutosave.saveOnLeave(buildingRoom, this);
        }
        buildingRoom = room;
        applyState(State.BUILD);
    }

    public RoomTemplate getBuildingRoom() {
        return buildingRoom;
    }

    public boolean isPlacingEntrance() {
        return placingEntrance;
    }

    public void setPlacingEntrance(boolean placingEntrance) {
        this.placingEntrance = placingEntrance;
    }

    public boolean isPlacingExit() {
        return placingExit;
    }

    public void setPlacingExit(boolean placingExit) {
        this.placingExit = placingExit;
    }

    public Connector getPendingExit() {
        return pendingExit;
    }

    public void setPendingExit(Connector pendingExit) {
        this.pendingExit = pendingExit;
    }

    public void clearBuildToolState() {
        placingEntrance = false;
        placingExit = false;
        pendingExit = null;
    }

    public void setGame(Game game){
        this.game = game;
    }

    public Game getGame(){
        if(currentState == State.INGAME){
            return game;
        } else {
            return null;
        }
    }

    public boolean isInGame(){
        if(game != null && currentState == State.INGAME){
            return true;
        }
        return false;
    }
}
