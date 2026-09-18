package org.indigo.sponge;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CancelledEvents implements Listener {


//    @EventHandler (priority = EventPriority.HIGHEST)
//    public void onBlockPhysics(BlockPhysicsEvent event) {
//
//        event.setCancelled(true);
//        event.getBlock().setBlockData(event.getBlock().getBlockData(),false);
//    }

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

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(Sponge.players.get(event.getPlayer()).getState() == SpongePlayer.State.LOBBY)
            event.setCancelled(true);
//        if(event.getClickedBlock().getType() == Material.NOTE_BLOCK) {
//            event.setCancelled(true);
//        }

    }

    @EventHandler
    public void onHunger(EntityExhaustionEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void NotePlayerEvent(NotePlayEvent event){
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event){

//        if(event.getBlock().getType() == Material.NOTE_BLOCK){
//            event.setCancelled(true);
//        }
    }
}
