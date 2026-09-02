package org.indigo.sponge.rooms;

import com.google.gson.Gson;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
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
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.indigo.sponge.Sponge.*;


public class Room {
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String name;
    private Vector minLoc;
    private Vector maxLoc;
    private boolean hasSchematic = false;

    public boolean isHasSchematic(){
        return hasSchematic;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    private RoomType roomType;
    private String floor;

    private List<Entrance> entrances = new ArrayList<>();
    private List<Exit> exits = new ArrayList<>();

    public Room(String name) {
        slimeWorld = asp.createEmptyWorld(name, false, new SlimePropertyMap(), loader);
        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();

        minLoc = new Vector(0, 0, 0);
        maxLoc = new Vector(0, 0, 0);
        roomType = RoomType.NORMAL;
        floor = "none";
        this.name = name;
        for (int x = 0; x < 500; x++) {
            for (int z = 0; z < 500; z++) {
                new Location(world, x, 50, z).getBlock().setType(Material.BEDROCK);
            }
        }
        rooms.put(name, this);
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        rooms.put(name, rooms.remove(this.name));
        this.name = name;
    }

    public void tpToWorld(Player player) {
        if (!asp.worldLoaded(slimeWorld)) {
            loadWorld();
        }
        player.teleport(new Location(world, 0, 51, 0));
    }

    public void loadWorld() {
        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld, false);
        world = worldInstance.getBukkitWorld();
        respawnPortalDisplays();
    }

    public void saveToFile() throws IOException {
        Gson gson = new Gson();
        Path path = Path.of("rooms/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, gson.toJson(this));
    }

    public Room() {
    }

    public static Room fromFile(String fileName) throws IOException, CorruptedWorldException, NewerFormatException, UnknownWorldException {
        Room room = new Gson().fromJson(Files.readString(Path.of(fileName)), Room.class);
        room.slimeWorld = asp.readWorld(loader, room.name, false, new SlimePropertyMap());
        SlimeWorldInstance worldInstance = asp.loadWorld(room.slimeWorld, false);
        room.world = worldInstance.getBukkitWorld();
        rooms.put(room.name, room);
        room.respawnPortalDisplays();
        return room;
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
        minLoc = new Vector(minX, minY, minZ);
        maxLoc = new Vector(maxX, maxY, maxZ);

        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world) , BlockVector3.at(minX,minY,minZ),BlockVector3.at(maxX,maxY,maxZ));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()
        );
        Operations.complete(forwardExtentCopy);

        File file = Path.of("rooms/" + name + ".schem").toFile();

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
        hasSchematic = true;
    }

    public World getWorld() {
        return world;
    }


    public List<Entrance> getEntrances() {
        if (entrances == null) {
            entrances = new ArrayList<>();
        }
        return entrances;
    }

    public Entrance addEntrance(Entrance entrance) {
        getEntrances().add(entrance);
        return entrance;
    }

    public boolean removeEntrance(UUID id) {
        Entrance found = findEntranceById(id);
        if (found == null) return false;
        found.removeDisplayEntity();
        return getEntrances().remove(found);
    }

    public Entrance findEntranceById(UUID id) {
        for (Entrance entrance : getEntrances()) {
            if (entrance.getId().equals(id)) return entrance;
        }
        return null;
    }

    public Entrance findEntranceAt(Location loc) {
        for (Entrance entrance : getEntrances()) {
            if (entrance.contains(loc)) return entrance;
        }
        return null;
    }

    public List<Exit> getExits() {
        if (exits == null) {
            exits = new ArrayList<>();
        }
        return exits;
    }

    public Exit addExit(Exit exit) {
        getExits().add(exit);
        return exit;
    }

    public boolean removeExit(UUID id) {
        Exit found = findExitById(id);
        if (found == null) return false;
        found.removeDisplayEntity();
        return getExits().remove(found);
    }

    public Exit findExitById(UUID id) {
        for (Exit exit : getExits()) {
            if (exit.getId().equals(id)) return exit;
        }
        return null;
    }

    public Exit findExitAt(Location loc) {
        for (Exit exit : getExits()) {
            if (exit.contains(loc)) return exit;
        }
        return null;
    }

    public void respawnPortalDisplays() {
        for (Entrance entrance : getEntrances()) {
            entrance.spawnDisplayEntity();
        }
        for (Exit exit : getExits()) {
            exit.spawnDisplayEntity();
        }
    }
}
