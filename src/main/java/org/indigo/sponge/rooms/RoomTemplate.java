package org.indigo.sponge.rooms;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infernalsuite.asp.api.events.LoadSlimeWorldEvent;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.indigo.sponge.Sponge.*;

public class RoomTemplate {
    public enum RoomType{
        NORMAL,
        HARD,
        SECRET
    }

    public String name;
    public List<Connector> exitConnectors = new ArrayList<>();
    public Connector entranceConnector;
    public boolean hasSchematic = false;
    private transient BoundingBox localBounds;
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private RoomType roomType;
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String floorName;
    private transient SlimeWorldInstance worldInstance;
    private static final Vector3 ENTRANCE_LOCAL_DIRECTION = Vector3.at(-1, 0, 0);

    private static final Gson GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override public boolean shouldSkipClass(Class<?> c) {
                    String n = c.getName();
                    return n.startsWith("org.bukkit.craftbukkit")
                            || n.startsWith("net.minecraft")
                            || org.bukkit.World.class.isAssignableFrom(c)
                            || org.bukkit.entity.Entity.class.isAssignableFrom(c)
                            || org.bukkit.Location.class.isAssignableFrom(c)
                            || com.infernalsuite.asp.api.world.SlimeWorld.class.isAssignableFrom(c);
                }
                @Override public boolean shouldSkipField(FieldAttributes f) { return false; }
            })
            .setPrettyPrinting()
            .create();

    /**
     * Creates a new room template: registers it under the given floor, creates a
     * fresh SlimeWorld for it, and lays down a bedrock floor to build on.
     * @param name unique name identifying this room template
     * @param floorName name of the floor this room belongs to
     * @param roomType difficulty/category of this room
     */
    public RoomTemplate(String name, String floorName, RoomType roomType){
        this.roomType = roomType;
        this.name = name;

        this.floorName = floorName;
        floors.get(floorName).addRoom(this);
        allRooms.put(name,this);

        slimeWorld = asp.createEmptyWorld(name, false, new SlimePropertyMap(), loader);
        this.worldInstance = asp.loadWorld(slimeWorld, false);

        world = worldInstance.getBukkitWorld();
        localBounds = new BoundingBox(0,0,0,0,0,0);
        for (int x = 0; x < 500; x++) {
            for (int z = 0; z < 500; z++) {
                new Location(world, x, 50, z).getBlock().setType(Material.BEDROCK);
            }
        }
    }

    /**
     * No-arg constructor used by Gson when deserializing a saved room template.
     */
    public RoomTemplate() {
    }

    /**
     * Removes the display entities for this room's entrance and all exits.
     */
    public void unload(){
        for(Connector exit : exitConnectors){
            exit.removeDisplayEntity();
        }
        entranceConnector.removeDisplayEntity();
    }

    /**
     * Spawns the display entities for all of this room's exits and its entrance,
     * if one is set.
     */
    public void spawnDisplays() {
        for (Connector c : exitConnectors) c.spawnDisplayEntity();
        if (entranceConnector != null) entranceConnector.spawnDisplayEntity();
    }

    /**
     * Teleports a player into this room's build world and shows its connector displays.
     * @param player the player to teleport
     */
    public void tpToWorld(Player player) {
        player.teleport(new Location(world, 0, 51, 0));
        spawnDisplays();
    }

    /**
     * Scans this room's build world for the extent of placed blocks, updates the
     * template's local bounds, and exports the room as a schematic file.
     * @throws IOException if writing the schematic file fails
     */
    public void updateBounds() throws IOException {
        unload();
        int minX = 600, minY = 600, minZ = 600;
        int maxX = -1000, maxY = -1000, maxZ = -1000;
        for (int x = 0; x < 500; x++) {
            for (int z = 0; z < 500; z++) {
                for (int y = 51; y < 130; y++) {
                    Location loc = new Location(world, x, y, z);
                    if (loc.getBlock().getType() != Material.AIR) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        minZ = Math.min(minZ, z);

                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                        maxZ = Math.max(maxZ, z);
                    }
                }
            }
        }

        localBounds.resize(minX,minY,minZ,maxX,maxY,maxZ);

        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world) , BlockVector3.at(minX,minY,minZ),BlockVector3.at(maxX,maxY,maxZ));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(BlockVector3.at(entranceConnector.getCenter().getX(),entranceConnector.getCenter().getY(),entranceConnector.getCenter().getZ()));
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint());

        Operations.complete(forwardExtentCopy);

        File file = Path.of("room_templates/" + name + ".schem").toFile();

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
        spawnDisplays();
        hasSchematic = true;
    }

    /**
     * Serializes this room template to a JSON file under room_templates/.
     * @throws IOException if writing the file fails
     */
    public void saveToFile() throws IOException {
        syncBoundsToFields();
        Path path = Path.of("room_templates/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(this));
    }

    /**
     * Loads a room template from its JSON file, reattaches its SlimeWorld, and
     * rebuilds its runtime (non-persisted) state.
     * @param roomName the name of the room template to load
     * @return the reconstructed RoomTemplate
     * @throws IOException if reading the file fails
     * @throws CorruptedWorldException if the associated world data is corrupted
     * @throws NewerFormatException if the world data format is unsupported
     * @throws UnknownWorldException if the associated world cannot be found
     */
    public static RoomTemplate fromFile(String roomName) throws IOException, CorruptedWorldException, NewerFormatException, UnknownWorldException {
        Path path = Path.of("room_templates/" + roomName + ".json");
        RoomTemplate room = GSON.fromJson(Files.readString(path), RoomTemplate.class);
        room.slimeWorld = asp.readWorld(loader, room.name, false, new SlimePropertyMap());
        room.worldInstance = asp.loadWorld(room.slimeWorld, true);
        room.world = room.worldInstance.getBukkitWorld();
        room.rebuildBounds();
        floors.get(room.floorName).addRoom(room);
        allRooms.put(room.name, room);
        for (Connector c : room.exitConnectors) c.rebuild(room.world);
        if (room.entranceConnector != null) room.entranceConnector.rebuild(room.world);
        return room;
    }

    /**
     * Copies the current local bounding box into the persisted min/max fields,
     * in preparation for saving.
     */
    private void syncBoundsToFields() {
        minX = localBounds.getMinX(); minY = localBounds.getMinY(); minZ = localBounds.getMinZ();
        maxX = localBounds.getMaxX(); maxY = localBounds.getMaxY(); maxZ = localBounds.getMaxZ();
    }

    /**
     * Reconstructs the local bounding box from the persisted min/max fields,
     * after loading.
     */
    private void rebuildBounds() {
        localBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Builds a WorldEdit rotation transform for the given yaw rotation.
     * @param degrees rotation in degrees around the Y axis
     * @return a transform representing that rotation
     */
    public static Transform rotationTransform(int degrees) {
        return new AffineTransform().rotateY(degrees);
    }

    /**
     * Rotates a direction vector by the given transform, ignoring any translation
     * component of the transform.
     * @param transform the transform whose rotation should be applied
     * @param dir the local-space direction vector to rotate
     * @return the rotated direction vector
     */
    public static Vector3 rotateDirection(Transform transform, Vector3 dir) {
        Vector3 zero = transform.apply(Vector3.ZERO);
        return transform.apply(dir).subtract(zero);
    }

    /**
     * Predicts the world-space bounding box this template would occupy if pasted
     * with the given transform and local origin, landing at pasteAt, without
     * touching any blocks.
     * @param transform the rotation transform to apply
     * @param localOrigin the local-space anchor point the paste is relative to
     * @param pasteAt the world-space location the anchor point should land at
     * @return the predicted world-space bounding box
     */
    public BoundingBox computeWorldBounds(Transform transform, BlockVector3 localOrigin, BlockVector3 pasteAt) {
        Vector3 originV = localOrigin.toVector3();
        Vector3 pasteV = pasteAt.toVector3();

        Vector3 min = transform.apply(Vector3.at(minX, minY, minZ).subtract(originV)).add(pasteV);
        Vector3 max = transform.apply(Vector3.at(maxX, maxY, maxZ).subtract(originV)).add(pasteV);

        Vector3 lo = min.getMinimum(max);
        Vector3 hi = min.getMaximum(max);

        return new BoundingBox(
                Math.floor(lo.x()), Math.floor(lo.y()), Math.floor(lo.z()),
                Math.floor(hi.x()) + 1, Math.floor(hi.y()) + 1, Math.floor(hi.z()) + 1
        );
    }

    /**
     * Computes the rotation (0/90/180/270 degrees) needed to rotate the fixed
     * entrance direction to face the given target direction.
     * @param targetDirection the direction the entrance should end up facing
     * @return the required rotation in degrees
     */
    public static int rotationToFace(Vector3 targetDirection) {
        double fromAngle = Math.atan2(ENTRANCE_LOCAL_DIRECTION.z(), ENTRANCE_LOCAL_DIRECTION.x());
        double toAngle = Math.atan2(targetDirection.z(), targetDirection.x());
        double deltaDeg = Math.toDegrees(fromAngle - toAngle);
        deltaDeg = ((deltaDeg % 360) + 360) % 360;
        return (int) Math.round(deltaDeg / 90.0) * 90 % 360;
    }
}