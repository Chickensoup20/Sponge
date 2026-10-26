package org.indigo.sponge.game;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.game.rooms.Connector;
import org.indigo.sponge.registries.FloorRegistry;
import org.indigo.sponge.registries.RoomRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.indigo.sponge.Sponge.*;

public class RoomTemplate {
    public enum RoomType implements RoomNode {
        NORMAL,
        HARD,
        SECRET,
        BOSS,
        BRANCH,
        INTRO
    }

    public String name;
    public List<Connector> exitConnectors = new ArrayList<>();
    public Connector entranceConnector;
    public boolean hasSchematic = false;
    private transient BoundingBox localBounds;
    private double minX, minY, minZ, maxX, maxY, maxZ;
    public RoomType roomType;
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String floorName;
    private transient SlimeWorldInstance worldInstance;
    private static final Vector3 ENTRANCE_LOCAL_DIRECTION = Vector3.at(-1, 0, 0);
    private static final Vector3 BACKWARD_DIRECTION = Vector3.at(-1, 0, 0);


    private static final Gson GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipClass(Class<?> c) {
                    String n = c.getName();
                    return n.startsWith("org.bukkit.craftbukkit")
                            || n.startsWith("net.minecraft")
                            || org.bukkit.World.class.isAssignableFrom(c)
                            || org.bukkit.entity.Entity.class.isAssignableFrom(c)
                            || org.bukkit.Location.class.isAssignableFrom(c)
                            || com.infernalsuite.asp.api.world.SlimeWorld.class.isAssignableFrom(c);
                }

                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return false;
                }
            })
            .setPrettyPrinting()
            .create();

    public RoomTemplate(String name, String floorName, RoomType roomType) {
        this.roomType = roomType;
        this.name = name;

        this.floorName = floorName;
        FloorRegistry.get(floorName).addRoom(this);
        RoomRegistry.register(this);

        slimeWorld = asp.createEmptyWorld(name, false, new SlimePropertyMap(), loader);
        this.worldInstance = asp.loadWorld(slimeWorld, false);

        world = worldInstance.getBukkitWorld();
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        localBounds = new BoundingBox(0, 0, 0, 0, 0, 0);
        for (int x = 0; x < 500; x++) {
            for (int z = 0; z < 500; z++) {
                new Location(world, x, 50, z).getBlock().setType(Material.BEDROCK);
            }
        }

    }

    public RoomTemplate() {
    }

    public void unload() {
        for (Connector exit : exitConnectors) {
            exit.removeDisplayEntity();
        }
        if (entranceConnector != null) entranceConnector.removeDisplayEntity();
    }

    public void spawnDisplays() {
        for (Connector c : exitConnectors) c.spawnDisplayEntity();
        if (entranceConnector != null) entranceConnector.spawnDisplayEntity();
    }

    public void tpToWorld(Player player) {
        player.teleport(new Location(world, 0, 51, 0));
        spawnDisplays();
    }

    // transient: Gson would otherwise persist these, and a room saved mid-save could never save again
    private final transient AtomicBoolean saving = new AtomicBoolean(false);
    private final transient AtomicBoolean savingWorld = new AtomicBoolean(false);

    public CompletableFuture<Void> saveAsync(boolean withSchematic) {
        if (!withSchematic || entranceConnector == null) return saveWorldAsync();
        // schematic first so the JSON picks up the freshly scanned bounds
        return saveSchematicAsync()
                .exceptionally(e -> {
                    Sponge.plugin.getLogger().warning("Schematic export failed for room " + name + ": " + e.getMessage());
                    return null;
                })
                .thenCompose(v -> saveWorldAsync());
    }

    public CompletableFuture<Void> saveWorldAsync() {
        if (!savingWorld.compareAndSet(false, true))
            return CompletableFuture.completedFuture(null); // a save is already running

        SlimeWorld copy;
        try {
            saveToFile();
            copy = worldInstance.getSerializableCopy();
        } catch (Exception e) {
            savingWorld.set(false);
            return CompletableFuture.failedFuture(e);
        }

        CompletableFuture<Void> result = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(Sponge.plugin, () -> {
            Exception error = null;
            try {
                asp.saveWorld(copy);
            } catch (Exception e) {
                error = e;
            }
            Exception finalError = error;
            Bukkit.getScheduler().runTask(Sponge.plugin, () -> {
                savingWorld.set(false);
                if (finalError != null) result.completeExceptionally(finalError);
                else result.complete(null);
            });
        });
        return result;
    }

    public void saveBlocking() throws IOException {
        saveToFile();
        asp.saveWorld(worldInstance.getSerializableCopy());
    }

    private CompletableFuture<List<ChunkSnapshot>> snapshotChunks(int perTick) {
        CompletableFuture<List<ChunkSnapshot>> done = new CompletableFuture<>();
        List<ChunkSnapshot> snaps = new ArrayList<>();
        int total = 32 * 32;

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                for (int n = 0; n < perTick && i < total; n++, i++) {
                    Chunk chunk = world.getChunkAt(i / 32, i % 32);
                    snaps.add(chunk.getChunkSnapshot(false, false, false));
                }
                if (i >= total) {
                    cancel();
                    done.complete(snaps);
                }
            }
        }.runTaskTimer(Sponge.plugin, 0L, 1L);

        return done;
    }

    public CompletableFuture<Void> saveSchematicAsync() {
        if (entranceConnector == null)
            return CompletableFuture.failedFuture(new IllegalStateException("Room has no entrance"));
        if (!saving.compareAndSet(false, true))
            return CompletableFuture.completedFuture(null); // a save is already running

        //remove displays, snapshot the chunks
        unload();
        Vector origin = entranceConnector.getCenter();

        CompletableFuture<Void> result = new CompletableFuture<>();

        //scan, copy, write
        snapshotChunks(16).thenAccept(snaps -> Bukkit.getScheduler().runTaskAsynchronously(Sponge.plugin, () -> {
            try {
                int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
                for (ChunkSnapshot s : snaps) {
                    for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 51; y < 130; y++) {
                        if (s.getBlockType(x, y, z).isAir()) continue;
                        int wx = s.getX() * 16 + x, wz = s.getZ() * 16 + z;
                        minX = Math.min(minX, wx); minY = Math.min(minY, y); minZ = Math.min(minZ, wz);
                        maxX = Math.max(maxX, wx); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, wz);
                    }
                }
                if (minX == Integer.MAX_VALUE) throw new IllegalStateException("Room is empty");

                CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world),
                        BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                clipboard.setOrigin(BlockVector3.at(origin.getX(), origin.getY(), origin.getZ()));
                Operations.complete(new ForwardExtentCopy(BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()));

                Path target = Path.of("room_templates", name + ".schem");
                Files.createDirectories(target.getParent());
                Path tmp = target.resolveSibling(name + ".schem.tmp");
                try (ClipboardWriter w = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(Files.newOutputStream(tmp))) {
                    w.write(clipboard);
                }
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

                //apply results
                int[] b = {minX, minY, minZ, maxX, maxY, maxZ};
                Bukkit.getScheduler().runTask(Sponge.plugin, () -> {
                    localBounds.resize(b[0], b[1], b[2], b[3], b[4], b[5]);
                    hasSchematic = true;
                    spawnDisplays();
                    saving.set(false);
                    result.complete(null);
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(Sponge.plugin, () -> {
                    spawnDisplays();
                    saving.set(false);
                    result.completeExceptionally(e);
                });
            }
        }));
        return result;
    }

    public void saveToFile() throws IOException {
        syncBoundsToFields();
        Path path = Path.of("room_templates/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(this));
    }

    public static RoomTemplate fromFile(String roomName) throws IOException, CorruptedWorldException, NewerFormatException, UnknownWorldException {
        Path path = Path.of("room_templates/" + roomName + ".json");
        RoomTemplate room = GSON.fromJson(Files.readString(path), RoomTemplate.class);
        room.slimeWorld = asp.readWorld(loader, room.name, false, new SlimePropertyMap());
        room.worldInstance = asp.loadWorld(room.slimeWorld, true);
        room.world = room.worldInstance.getBukkitWorld();
        room.world.setGameRule(GameRules.ADVANCE_TIME, false);
        room.rebuildBounds();
        FloorRegistry.get(room.floorName).addRoom(room);
        for (Connector c : room.exitConnectors) c.rebuild(room.world);
        if (room.entranceConnector != null) room.entranceConnector.rebuild(room.world);
        return room;
    }

    private void syncBoundsToFields() {
        minX = localBounds.getMinX();
        minY = localBounds.getMinY();
        minZ = localBounds.getMinZ();
        maxX = localBounds.getMaxX();
        maxY = localBounds.getMaxY();
        maxZ = localBounds.getMaxZ();
    }

    private void rebuildBounds() {
        localBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }


    public static Transform rotationTransform(int degrees) {
        return new AffineTransform().rotateY(degrees);
    }


    public static Vector3 rotateDirection(Transform transform, Vector3 dir) {
        Vector3 zero = transform.apply(Vector3.ZERO);
        return transform.apply(dir).subtract(zero);
    }

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

    public static int rotationToFace(Vector3 targetDirection) {
        double fromAngle = Math.atan2(ENTRANCE_LOCAL_DIRECTION.z(), ENTRANCE_LOCAL_DIRECTION.x());
        double toAngle = Math.atan2(targetDirection.z(), targetDirection.x());
        double deltaDeg = Math.toDegrees(fromAngle - toAngle);
        deltaDeg = ((deltaDeg % 360) + 360) % 360;
        return (int) Math.round(deltaDeg / 90.0) * 90 % 360;
    }

    public static boolean isBackwardDirection(Vector3 direction) {
        return direction.normalize().dot(BACKWARD_DIRECTION) > 0.99;
    }
}