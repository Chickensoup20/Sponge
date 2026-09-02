package org.indigo.sponge.rooms;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.indigo.sponge.SpongePlayer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.indigo.sponge.Sponge.*;

public class RoomEvents implements Listener {

    private static final Color ENTRANCE_GLOW_COLOR = Color.ORANGE;
    private static final Color EXIT_GLOW_COLOR = Color.fromRGB(0x87CEEB); // sky blue

    private static final Material ENTRANCE_MATERIAL = Material.ORANGE_STAINED_GLASS;
    private static final Material EXIT_MATERIAL = Material.LIGHT_BLUE_STAINED_GLASS;


    private static class PendingSelection {
        Location corner1;
        Location corner2;
        BlockDisplay previewEntity;
    }

    private final Map<UUID, PendingSelection> pendingEntrances = new HashMap<>();
    private final Map<UUID, PendingSelection> pendingExits = new HashMap<>();

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (event.getClickedBlock() == null) return;

        boolean isEntranceWand = item.isSimilar(entranceWand);
        boolean isExitWand = item.isSimilar(exitWand);
        if (!isEntranceWand && !isExitWand) return;

        Player player = event.getPlayer();
        SpongePlayer sPlayer = playerStates.get(player);
        if (sPlayer == null || sPlayer.getState() != SpongePlayer.State.BUILD) return;

        Room room = sPlayer.getBuildingRoom();
        if (room == null) {
            player.sendMessage("You're not currently building a room.");
            return;
        }

        event.setCancelled(true);
        if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleRemoval(player, room, event.getClickedBlock().getLocation(), isEntranceWand);
            return;
        }

        if (isEntranceWand) {
            handleSelectionClick(event, player, room, pendingEntrances, ENTRANCE_MATERIAL, ENTRANCE_GLOW_COLOR, true);
        } else {
            handleSelectionClick(event, player, room, pendingExits, EXIT_MATERIAL, EXIT_GLOW_COLOR, false);
        }
    }

    private void handleRemoval(Player player, Room room, Location clicked, boolean isEntranceWand) {
        if (isEntranceWand) {
            Entrance found = room.findEntranceAt(clicked);
            if (found != null) {
                room.removeEntrance(found.getId());
                player.sendMessage("Removed entrance.");
            }
        } else {
            Exit found = room.findExitAt(clicked);
            if (found != null) {
                room.removeExit(found.getId());
                player.sendMessage("Removed exit.");
            }
        }
    }

    private void handleSelectionClick(PlayerInteractEvent event, Player player, Room room,
                                      Map<UUID, PendingSelection> pendingMap,
                                      Material material, Color glowColor,
                                      boolean isEntrance) {

        UUID uuid = player.getUniqueId();
        Location clicked = event.getClickedBlock().getLocation();
        PendingSelection selection = pendingMap.computeIfAbsent(uuid, k -> new PendingSelection());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.corner1 = clicked;
            if (selection.previewEntity == null || !selection.previewEntity.isValid()) {
                selection.previewEntity = spawnPreview(clicked, material, glowColor);
            } else {
                selection.previewEntity.teleport(clicked);
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.corner2 = clicked;
            if (selection.previewEntity == null || !selection.previewEntity.isValid()) {
                Location spawnAt = selection.corner1 != null ? selection.corner1 : clicked;
                selection.previewEntity = spawnPreview(spawnAt, material, glowColor);
            }
        }

        if (selection.corner1 != null && selection.corner2 != null && selection.previewEntity != null) {
            resizeToFit(selection.previewEntity, selection.corner1, selection.corner2);
            finalizeSelection(player, room, pendingMap, selection, isEntrance);
        }
    }

    private void finalizeSelection(Player player, Room room, Map<UUID, PendingSelection> pendingMap,
                                   PendingSelection selection, boolean isEntrance) {
        if (isEntrance) {
            Entrance entrance = new Entrance(selection.corner1, selection.corner2);
            entrance.setDisplayEntity(selection.previewEntity);
            room.addEntrance(entrance);
            player.sendMessage("Entrance created.");
        } else {
            Exit exit = new Exit(selection.corner1, selection.corner2);
            exit.setDisplayEntity(selection.previewEntity);
            room.addExit(exit);
            player.sendMessage("Exit created.");
        }
        pendingMap.remove(player.getUniqueId());
    }

    private BlockDisplay spawnPreview(Location loc, Material material, Color glowColor) {
        BlockDisplay entity = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        entity.setBlock(Bukkit.createBlockData(material));
        entity.setGlowing(true);
        entity.setGlowColorOverride(glowColor);
        return entity;
    }

    private void resizeToFit(BlockDisplay entity, Location a, Location b) {
        double x1 = Math.floor(Math.min(a.x(), b.x()));
        double y1 = Math.floor(Math.min(a.y(), b.y()));
        double z1 = Math.floor(Math.min(a.z(), b.z()));
        double x2 = Math.floor(Math.max(a.x(), b.x()));
        double y2 = Math.floor(Math.max(a.y(), b.y()));
        double z2 = Math.floor(Math.max(a.z(), b.z()));

        float sizeX = (float) (x2 - x1 + 1);
        float sizeY = (float) (y2 - y1 + 1);
        float sizeZ = (float) (z2 - z1 + 1);

        entity.teleport(new Location(a.getWorld(), x1, y1, z1));
        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(sizeX, sizeY, sizeZ),
                new Quaternionf()
        ));
    }
}
