package org.indigo.sponge;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GamePlayer{
    private final Player player;
    private State currentState;
    public enum State{
        LOBBY,
        INGAME,
        DEV
    }
    public GamePlayer(Player player, State state){
        player.sendMessage("1");
        currentState = state;
        this.player = player;

    }

    public void applyState(){

            player.teleport(new Location(Bukkit.getWorld("lobby"), 64, 67, 127));
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("hi");
    }


    public void applyState(State state){
        setCurrentState(state);
        applyState();
    }

    private void setCurrentState(State state){
        currentState = state;

    }
}
