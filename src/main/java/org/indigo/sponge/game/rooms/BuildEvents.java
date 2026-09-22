package org.indigo.sponge.game.rooms;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.registries.Items;
import org.indigo.sponge.registries.Players;

import java.util.List;

public class BuildEvents implements Listener {

    /**
     * Handles player interactions while in BUILD state: left/right-clicking with
     * the entrance wand defines a room's entrance connector, and doing the same
     * with the exit wand defines (or, while sneaking, removes) exit connectors.
     * @param event the player interact event to handle
     */
    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        SpongePlayer session = Players.find(event.getPlayer());
        if (session == null || session.getState() != SpongePlayer.State.BUILD) {
            return;
        }
        RoomTemplate room = session.getBuildingRoom();
        if (room == null || event.getItem() == null || event.getClickedBlock() == null) {
            return;
        }
        Location clickedBlock = event.getClickedBlock().getLocation();
        if (event.getItem().isSimilar(Items.entranceWand)) {
            event.setCancelled(true);
            Connector entrance = room.entranceConnector;

            if (!session.isPlacingEntrance()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (entrance == null) {
                        entrance = new Connector(clickedBlock, clickedBlock, Connector.ConnectorType.ENTRANCE, new Vector(1, 0, 0));
                    } else {
                        entrance.setCorner1(clickedBlock);
                    }
                    entrance.setCorner2(clickedBlock);
                    entrance.spawnDisplayEntity();
                    room.entranceConnector = entrance;
                    session.setPlacingEntrance(true);
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                entrance.setCorner2(event.getClickedBlock().getLocation());
                entrance.spawnDisplayEntity();
                session.setPlacingEntrance(false);
            }
        }
        if (event.getItem().isSimilar(Items.exitWand)) {
            event.setCancelled(true);
            List<Connector> exits = room.exitConnectors;
            if (event.getPlayer().isSneaking()) {
                for (Connector connector : room.exitConnectors) {
                    if (connector.boundingBox.contains(clickedBlock.getBlockX() + 0.51, clickedBlock.getBlockY() + 0.5, clickedBlock.getBlockZ() + 0.5)) {
                        connector.removeDisplayEntity();
                        exits.remove(connector);
                        return;
                    }
                }
            }
            if (!session.isPlacingExit()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Connector exit = new Connector(clickedBlock, clickedBlock, Connector.ConnectorType.EXIT, event.getBlockFace().getDirection());
                    exit.setCorner2(clickedBlock);
                    exit.spawnDisplayEntity();
                    session.setPendingExit(exit);
                    session.setPlacingExit(true);
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Connector exit = session.getPendingExit();
                exit.setCorner2(event.getClickedBlock().getLocation());
                exit.spawnDisplayEntity();
                room.exitConnectors.add(exit);
                session.setPendingExit(null);
                session.setPlacingExit(false);
            }
        }
    }
}
