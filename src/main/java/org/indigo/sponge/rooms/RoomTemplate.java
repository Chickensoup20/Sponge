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
    private transient BoundingBox localBounds;
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private RoomType roomType;
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String floorName;
    public boolean hasSchematic = false;
    private transient SlimeWorldInstance worldInstance;


    public RoomTemplate(String name, String floorName, RoomType roomType){
        this.roomType = roomType;
        this.name = name;

        this.floorName = floorName;
        floors.get(floorName).addRoom(this);
        allRooms.put(name,this);

        //World Creation
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

    public void unload(){
        for(Connector exit : exitConnectors){
            exit.removeDisplayEntity();
        }
        entranceConnector.removeDisplayEntity();
    }




    //Persistence
    private void syncBoundsToFields() {
        minX = localBounds.getMinX(); minY = localBounds.getMinY(); minZ = localBounds.getMinZ();
        maxX = localBounds.getMaxX(); maxY = localBounds.getMaxY(); maxZ = localBounds.getMaxZ();
    }

    private void rebuildBounds() {
        localBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

    }

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

    public void saveToFile() throws IOException {
        syncBoundsToFields();
        Path path = Path.of("room_templates/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(this));
    }

    public RoomTemplate() {
    }

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

    public void spawnDisplays() {
        for (Connector c : exitConnectors) c.spawnDisplayEntity();
        if (entranceConnector != null) entranceConnector.spawnDisplayEntity();
    }

    public void tpToWorld(Player player) {
        player.teleport(new Location(world, 0, 51, 0));
        spawnDisplays();
    }

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

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()
        );
        Operations.complete(forwardExtentCopy);

        File file = Path.of("room_templates/" + name + ".schem").toFile();

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
        spawnDisplays();
        hasSchematic = true;

    }

}
