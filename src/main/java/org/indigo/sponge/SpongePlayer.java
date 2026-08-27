package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpongePlayer {
    private final Player player;
    private State currentState;

    public enum State {
        LOBBY,
        INGAME,
        DEV
    }

    public SpongePlayer(Player player, State state) {
        player.sendMessage("1");
        currentState = state;
        this.player = player;

    }

    public void applyState() {
        Location teleportLoc;
        switch (currentState){
            case DEV -> {
                teleportLoc = new Location(Bukkit.getWorld("lobby"), 64, 67, 127);
            }
            case LOBBY -> {
                teleportLoc = new Location(Bukkit.getWorld("lobby"), 64, 67, 127);
                player.setGameMode(GameMode.ADVENTURE);
            }
            case INGAME -> {
                teleportLoc = new Location(Bukkit.getWorld("lobby"), 64, 67, 127);
            }
        }
        player.getInventory().clear();


    }


    public void applyState(State state) {
        setCurrentState(state);
        applyState();
    }

    private void setCurrentState(State state) {
        currentState = state;

    }
}
