package org.indigo.sponge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ManagementEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
       GamePlayer state = new GamePlayer(event.getPlayer(), GamePlayer.State.LOBBY);
       state.applyState();
    }


}
