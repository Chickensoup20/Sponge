package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
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
                break;
            }

        }
        player.getInventory().clear();
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setSaturation(20);
        player.setFoodLevel(20);
        player.setFlySpeed(0.1f);
        player.teleport(new Location(Bukkit.getWorld("lobby"), 64.5, 67.5, 127.5));

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
}
