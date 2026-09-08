package org.indigo.sponge.rooms;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.SpongePlayer;

import java.util.List;

public class BuildEvents implements Listener {
    boolean newEntrance = false;
    boolean newExit = false;
    Connector exit;
    @EventHandler
    public void interactEvent(PlayerInteractEvent event){
        if(Sponge.players.get(event.getPlayer()).getState() == SpongePlayer.State.BUILD){
            RoomTemplate room = Sponge.players.get(event.getPlayer()).getBuildingRoom();
            if(event.getItem() != null){
                Location clickedBlock = event.getClickedBlock().getLocation();
                event.getPlayer().sendMessage(event.getBlockFace().getDirection().toString());
                if(event.getItem().isSimilar(Sponge.entranceWand)){
                    event.setCancelled(true);
                    Connector entrance = room.entranceConnector;

                    if(newEntrance == false){
                        if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (entrance == null) {
                                entrance = new Connector(clickedBlock, clickedBlock, Connector.ConnectorType.ENTRANCE,new Vector(1,0,0));
                            } else {
                                entrance.setCorner1(clickedBlock);
                            }
                            entrance.setCorner2(clickedBlock);
                            entrance.spawnDisplayEntity();
                            room.entranceConnector = entrance;
                            newEntrance = true;
                        }
                    } else if(newEntrance){
                        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            entrance.setCorner2(event.getClickedBlock().getLocation());
                            entrance.spawnDisplayEntity();
                            newEntrance = false;
                        }
                    }
                }
                if(event.getItem().isSimilar(Sponge.exitWand)){
                    event.setCancelled(true);
                    List<Connector> exits = room.exitConnectors;
                    if(event.getPlayer().isSneaking()){
                        for(Connector connector : room.exitConnectors){
                            if(connector.boundingBox.contains(clickedBlock.getBlockX()+0.51,clickedBlock.getBlockY()+0.5,clickedBlock.getBlockZ()+0.5)){
                                connector.removeDisplayEntity();
                                exits.remove(connector);
                                return;
                            }//
                        }
                    }
                    if(newExit == false){
                        if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            exit = new Connector(clickedBlock, clickedBlock, Connector.ConnectorType.EXIT,event.getBlockFace().getDirection());
                            exit.setCorner2(clickedBlock);
                            exit.spawnDisplayEntity();
                            newExit = true;
                        }
                    } else if(newExit){
                        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            exit.setCorner2(event.getClickedBlock().getLocation());
                            exit.spawnDisplayEntity();
                            room.exitConnectors.add(exit);
                            newExit = false;
                        }
                    }
                    event.getPlayer().sendMessage(exit.boundingBox.toString());
                }
            }
        }
    }
}
