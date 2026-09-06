package org.indigo.sponge.rooms;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.SpongePlayer;

public class BuildEvents implements Listener {
    boolean newEntrance = false;
    @EventHandler
    public void interactEvent(PlayerInteractEvent event){
        if(Sponge.players.get(event.getPlayer()).getState() == SpongePlayer.State.BUILD){
            RoomTemplate room = Sponge.players.get(event.getPlayer()).getBuildingRoom();
            if(event.getItem() != null){
                Location clickedBlock = event.getClickedBlock().getLocation();
                if(event.getItem().isSimilar(Sponge.entranceWand)){
                    event.setCancelled(true);
                    Connector entrance = room.entranceConnector;

                    if(newEntrance == false){

                        if(entrance == null){
                            entrance = new Connector(clickedBlock,clickedBlock, Connector.ConnectorType.ENTRANCE);
                        } else {
                            entrance.setCorner1(clickedBlock);
                        }
                        entrance.setCorner2(clickedBlock);
                        entrance.spawnDisplayEntity();
                        room.entranceConnector = entrance;
                        newEntrance = true;
                    } else if(newEntrance){
                        entrance.setCorner2(event.getClickedBlock().getLocation());
                        entrance.spawnDisplayEntity();
                        newEntrance = false;
                    }
                }
            }
        }
    }
}
