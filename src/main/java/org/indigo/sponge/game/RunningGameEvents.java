package org.indigo.sponge.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.indigo.sponge.game.rooms.Connector;

import static org.indigo.sponge.Sponge.*;

public class RunningGameEvents implements Listener {
    @EventHandler
    public void serverTick(ServerTickStartEvent event){
        for(Game game : runningGames){
            for(Connector exit : game.instances.getLast().exits){
                if(!exit.isBlocked()){
                    int amountPlayers = game.alivePlayers.size();
                    int count = 0;
                    for(Player player : game.alivePlayers){
                        if(exit.boundingBox.contains(player.getBoundingBox())){
                            count++;
                        }
                    }
                    if (count == amountPlayers){
                        game.nextRoom(exit);
                        break;
                    }
                }
            }
        }
    }

}
