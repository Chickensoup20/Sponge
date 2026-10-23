package org.indigo.sponge.game.rooms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.SpongePlayer;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.game.RoomTemplate;
import org.indigo.sponge.registries.Players;

import java.util.HashSet;
import java.util.Set;

/**
 * Periodically saves every room someone is currently building in, plus helpers
 * for saving when a builder leaves a room.
 */
public final class RoomAutosave {
    private static final long INTERVAL_TICKS = 20L * 60 * 5; // 5 minutes

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, RoomAutosave::saveActiveRooms, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    /**
     * Saves rooms with a builder in them. Rooms nobody is in haven't changed since
     * the builder left (which already saved them), so they're skipped.
     */
    private static void saveActiveRooms() {
        Set<RoomTemplate> active = new HashSet<>();
        for (SpongePlayer session : Players.all()) {
            if (session.getState() == SpongePlayer.State.BUILD && session.getBuildingRoom() != null) {
                active.add(session.getBuildingRoom());
            }
        }
        for (RoomTemplate room : active) {
            room.saveWorldAsync().whenComplete((v, err) -> {
                if (err != null) {
                    Sponge.plugin.getLogger().warning("Autosave failed for room " + room.name + ": " + err.getMessage());
                } else {
                    notifyBuilders(room, "<gray>Autosaved " + room.name);
                }
            });
        }
    }

    /**
     * Full save (schematic + world + JSON) for a room a builder just left.
     *
     * @param room   the room being left
     * @param leaver the builder leaving, told about the result if still online
     */
    public static void saveOnLeave(RoomTemplate room, SpongePlayer leaver) {
        room.saveAsync(true).whenComplete((v, err) -> {
            if (err != null) {
                Sponge.plugin.getLogger().warning("Save failed for room " + room.name + ": " + err.getMessage());
                if (leaver.getPlayer().isOnline()) Utils.sendSystemMessage(leaver.getPlayer(), "<red>Saving " + room.name + " failed: " + err.getMessage());
            } else if (leaver.getPlayer().isOnline()) {
                Utils.sendSystemMessage(leaver.getPlayer(), "Saved " + room.name);
            }
        });
    }

    private static void notifyBuilders(RoomTemplate room, String message) {
        for (SpongePlayer session : Players.all()) {
            if (session.getState() == SpongePlayer.State.BUILD && session.getBuildingRoom() == room) {
                session.getPlayer().sendActionBar(Sponge.mm.deserialize(message));
            }
        }
    }

    private RoomAutosave() {
    }
}
