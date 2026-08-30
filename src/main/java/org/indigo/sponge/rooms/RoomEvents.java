package org.indigo.sponge.rooms;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.SpongePlayer;

import static org.indigo.sponge.Sponge.*;

public class RoomEvents implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent event){
        event.getPlayer().sendMessage("hi");
        event.getPlayer().sendMessage(event.getItem().getItemMeta().getPersistentDataContainer().getKeys().toString());
        if(event.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey("sponge","entranceWand"))){

            ItemStack item = event.getItem();
            Player player = event.getPlayer();
            player.sendMessage("hi");
            SpongePlayer sPlayer = playerStates.get(player);
            if(event.getAction() == Action.LEFT_CLICK_BLOCK){
                if(sPlayer.getState() == SpongePlayer.State.BUILD){
                    Interaction ent = event.getInteractionPoint().getWorld().spawn(event.getInteractionPoint(), Interaction.class);
                    ent.setGlowing(true);
                }
            }
        }
    }
}
