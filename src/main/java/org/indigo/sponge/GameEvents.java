package org.indigo.sponge;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.http.WebSocket;

public class GameEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome du1de!");

        if ( !Sponge.joinedPlayers.contains(event.getPlayer()))
        {
            event.getPlayer().sendMessage("Welcome dude!");
        }
    }

}
