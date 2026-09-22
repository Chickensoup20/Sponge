package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.game.rooms.Connector;

import java.util.UUID;

public class SpongePlayer {
    private final UUID uuid;
    private Player player;
    private State currentState;
    private RoomTemplate buildingRoom;
    private boolean placingEntrance;
    private boolean placingExit;
    private Connector pendingExit;

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
            }
            case BUILD -> {
                player.setGameMode(GameMode.CREATIVE);
                return;
            }
            case INGAME -> {
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

    public State getState() {
        return currentState;
    }

    public void setBuilding(RoomTemplate room) {
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
}
