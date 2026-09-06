package org.indigo.sponge.rooms;

import com.google.gson.Gson;
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

    private String name;
    public List<Connector> exitConnectors = new ArrayList<>();
    public Connector entranceConnector;
    private transient BoundingBox localBounds;
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private RoomType roomType;
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String floorName;
    public boolean hasSchematic = false;


    public RoomTemplate(String name, String floorName, RoomType roomType){
        this.roomType = roomType;
        this.name = name;

        this.floorName = floorName;
        floors.get(floorName).addRoom(this);
        allRooms.put(name,this);

        //World Creation
        slimeWorld = asp.createEmptyWorld(name, false, new SlimePropertyMap(), loader);
        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        localBounds = new BoundingBox(0,0,0,0,0,0);
        for (int x = 0; x < 500; x++) {
            for (int z = 0; z < 500; z++) {
                new Location(world, x, 50, z).getBlock().setType(Material.BEDROCK);
            }
        }


    }


    //Persistence
    private void syncBoundsToFields() {
        minX = localBounds.getMinX(); minY = localBounds.getMinY(); minZ = localBounds.getMinZ();
        maxX = localBounds.getMaxX(); maxY = localBounds.getMaxY(); maxZ = localBounds.getMaxZ();
    }

    private void rebuildBounds() {
        localBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

    }

    public void saveToFile() throws IOException {
        syncBoundsToFields();
        Gson gson = new Gson();
        Path path = Path.of("room_templates/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, gson.toJson(this));
    }

    public RoomTemplate() {
    }

    public static RoomTemplate fromFile(String roomName) throws IOException, CorruptedWorldException, NewerFormatException, UnknownWorldException {
        Path path = Path.of("room_templates/" + roomName + ".json");
        RoomTemplate room = new Gson().fromJson(Files.readString(path), RoomTemplate.class);
        room.slimeWorld = asp.readWorld(loader, room.name, false, new SlimePropertyMap());
        SlimeWorldInstance worldInstance = asp.loadWorld(room.slimeWorld, false);
        room.world = worldInstance.getBukkitWorld();
        room.rebuildBounds();
        floors.get(room.floorName).addRoom(room);
        allRooms.put(room.name, room);
        return room;
    }

    public void tpToWorld(Player player) {
        player.teleport(new Location(world, 0, 51, 0));
    }

    public void updateBounds() throws IOException {
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
        hasSchematic = true;

    }

}
