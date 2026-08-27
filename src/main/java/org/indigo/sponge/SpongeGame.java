package org.indigo.sponge;

import cloud.emilys.fibre.api.annotation.event.Listen;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpongeGame {
    @Listen
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(Component.text("[Sponge] " + event.getPlayer().getName() + " joined the game."));
    }
}
