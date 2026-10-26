package org.indigo.sponge.game;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.registries.Games;
import org.indigo.sponge.registries.Players;

public class RunningGameEvents implements Listener {
    @EventHandler
    public void serverTick(ServerTickStartEvent event){
        for(Game game : Games.runningGames){
            int amountPlayers = game.amtAlivePlayers();
            if(amountPlayers == 0){
                game.end();
                continue;
            }
            for(Connector exit : game.instances.getLast().exits){
                if(!exit.isBlocked()){

                    int count = 0;
                    for(SpongePlayer player : game.getAlivePlayers()){
                        if(player.getState() == SpongePlayer.State.INGAME) {
                            if (exit.boundingBox.contains(player.getPlayer().getBoundingBox())) {
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

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        event.setCancelled(true);
        Player player = event.getPlayer();
        SpongePlayer spongePlayer = Players.get(player);
        if(spongePlayer.isInGame()){
            player.setGameMode(GameMode.SPECTATOR);
            Game game = spongePlayer.getGame();
            game.removeAlivePlayer(spongePlayer);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player player){
            if(Players.get(player).isInGame()){
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }
    }

}
