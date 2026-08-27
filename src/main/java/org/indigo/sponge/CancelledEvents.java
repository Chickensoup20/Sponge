package org.indigo.sponge;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class CancelledEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(Component.text("On my way!"));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {

        event.setCancelled(true);

        event.getBlock().setType(event.getBlock().getType(),false);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof FallingBlock fallingBlock)) return;

        Location loc = entity.getLocation();
        event.setCancelled(true);
        loc.getBlock().setBlockData(fallingBlock.getBlockData());
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
        }
    }

}
