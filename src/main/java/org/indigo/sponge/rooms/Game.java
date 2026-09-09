package org.indigo.sponge.rooms;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.indigo.sponge.Sponge.*;

public class Game {
    private List<Player> players = new ArrayList<>();
    private UUID uuid;
    private SlimeWorld slimeWorld;
    private World world;
    private List<BoundingBox> roomBoxes = new ArrayList<>();
    private List<RoomInstance> instances = new ArrayList<>();

    /**
     * Starts a new game instance for the given players: creates a dedicated
     * SlimeWorld and pastes the starting "test" room at the origin.
     * @param players the players participating in this game
     */
    public Game(List<Player> players){
        this.players = players;
        this.uuid = UUID.randomUUID();
        slimeWorld = asp.createEmptyWorld(uuid.toString(), false, new SlimePropertyMap(), loader);

        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        RoomInstance room = new RoomInstance(allRooms.get("test"));
        Location firstPaste = new Location(world, 0, 51, 0);
        room.generate(firstPaste, 0);
        roomBoxes.add(room.boundingBox);
        instances.add(room);
    }

    /**
     * Teleports all players in this game to the starting room.
     */
    public void start() {
        for (Player player : players)
            player.teleport(new Location(world, 0, 51, 0));
    }

    /**
     * Attempts to generate a new room from the given exit by searching all known
     * room templates for one that fits without overlapping existing rooms.
     * @param exit the exit connector to expand from
     */
    public void expandFrom(Connector exit) {
        if (exit.isBlocked()) return;

        List<RoomTemplate> pool = new ArrayList<>(allRooms.values());
        RoomInstance next = RoomInstance.generateFromExit(exit, world, pool, roomBoxes);
        if (next != null) {
            roomBoxes.add(next.boundingBox);
            instances.add(next);
        }
    }

    /**
     * Test helper: pastes a "test2" room off the first exit of the most recently
     * generated room, rotated to face that exit. If the resulting room would
     * overlap another already-placed room, the exit is sealed instead. On success,
     * every other exit on the previous room is sealed off.
     */
    public void nextRoom() {
        RoomInstance previousRoom = instances.get(instances.size() - 1);
        Connector exit = previousRoom.exits.get(0);

        if (exit.isBlocked()) return;

        Vector exitDirRaw = exit.getDirection();
        Vector3 exitDir = Vector3.at(exitDirRaw.getX(), 0, exitDirRaw.getZ()).normalize();
        Vector3 requiredEntranceFacing = exitDir.multiply(-1);

        int rotation = RoomTemplate.rotationToFace(requiredEntranceFacing);
        Transform transform = RoomTemplate.rotationTransform(rotation);

        RoomTemplate template = allRooms.get("test2");
        Vector entranceCenter = template.entranceConnector.getCenter();
        BlockVector3 localOrigin = BlockVector3.at(
                (int) Math.floor(entranceCenter.getX()),
                (int) Math.floor(entranceCenter.getY()),
                (int) Math.floor(entranceCenter.getZ()));

        Vector exitCenter = exit.getCenter();
        BlockVector3 pasteAt = BlockVector3.at(
                (int) Math.floor(exitCenter.getX()),
                (int) Math.floor(exitCenter.getY()),
                (int) Math.floor(exitCenter.getZ()));

        BoundingBox candidateBox = template.computeWorldBounds(transform, localOrigin, pasteAt);
        candidateBox.expand(1, 1, 1);

        boolean collides = instances.stream()
                .filter(inst -> inst != previousRoom)
                .map(inst -> inst.boundingBox)
                .anyMatch(existing -> existing.overlaps(candidateBox));

        if (collides) {
            exit.block(world);
            return;
        }

        Location pasteLoc = new Location(world, pasteAt.x(), pasteAt.y(), pasteAt.z());
        RoomInstance next = new RoomInstance(template);
        next.generate(pasteLoc, rotation);

        roomBoxes.add(next.boundingBox);
        instances.add(next);

        for (Connector otherExit : previousRoom.exits) {
            if (otherExit != exit && !otherExit.isBlocked()) {
                otherExit.block(world);
            }
        }
    }
}