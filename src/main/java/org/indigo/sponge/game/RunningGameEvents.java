package org.indigo.sponge.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.registries.Games;
import org.indigo.sponge.registries.Players;

public class RunningGameEvents implements Listener {
    @EventHandler
    public void serverTick(ServerTickStartEvent event){
        for(Game game : Games.runningGames){
            for(Connector exit : game.instances.getLast().exits){
                if(!exit.isBlocked()){
                    int amountPlayers = game.alivePlayers.size();
                    int count = 0;
                    for(Player player : game.alivePlayers){
                        if(Players.get(player).getState() == SpongePlayer.State.INGAME) {
                            if (exit.boundingBox.contains(player.getBoundingBox())) {
                                count++;
                            }
                        }
                    }
                    if (count == amountPlayers){
                        game.expandFrom(exit);
                        break;
                    }
                }
            }
        }
    }

}
