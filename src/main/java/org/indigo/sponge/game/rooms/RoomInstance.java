package org.indigo.sponge.game.rooms;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.indigo.sponge.game.RoomTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoomInstance {
    private RoomTemplate roomTemplate;
    private Location pasteLoc;
    public BoundingBox boundingBox;
    public List<Connector> exits = new ArrayList<>();
    public Connector entrance;

    
    public RoomInstance(RoomTemplate roomTemplate){
        this.roomTemplate = roomTemplate;
    }

    
    public void generate(Location pasteLoc, int rotation){
        this.pasteLoc = pasteLoc;

        File file = Path.of("room_templates/" + roomTemplate.name + ".schem").toFile();
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClipboardHolder holder = new ClipboardHolder(clipboard);
        if (rotation != 0) {
            holder.setTransform(new AffineTransform().rotateY(rotation));
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pasteLoc.getWorld()))) {
            Operation operation = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(pasteLoc.x(),pasteLoc.y(),pasteLoc.z()))
                    .build();
            Operations.complete(operation);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        BlockVector3 paste = BukkitAdapter.asBlockVector(pasteLoc);
        Transform transform = holder.getTransform();
        Region region = clipboard.getRegion();
        BlockVector3 origin = clipboard.getOrigin();

        Vector3 lo = null, hi = null;
        for (BlockVector3 corner : List.of(region.getMinimumPoint(), region.getMaximumPoint())) {
            Vector3 t = transform.apply(corner.subtract(origin).toVector3()).add(paste.toVector3());
            lo = lo == null ? t : lo.getMinimum(t);
            hi = hi == null ? t : hi.getMaximum(t);
        }

        boundingBox = new BoundingBox(
                Math.floor(lo.x()), Math.floor(lo.y()), Math.floor(lo.z()),
                Math.floor(hi.x()) + 1, Math.floor(hi.y()) + 1, Math.floor(hi.z()) + 1
        );

        exits = new ArrayList<>();
        for (Connector exitTemplate : roomTemplate.exitConnectors) {
            exits.add(exitTemplate.transformed(transform, origin, paste, pasteLoc.getWorld()));
        }
        if (roomTemplate.entranceConnector != null) {
            entrance = roomTemplate.entranceConnector.transformed(transform, origin, paste, pasteLoc.getWorld());
        }
    }

    
    public static RoomInstance generateFromExit(Connector exit, World world,
                                                List<RoomTemplate> candidatePool,
                                                List<BoundingBox> existingBoxes) {
        for (RoomTemplate candidate : candidatePool) {
            Connector entranceLocal = candidate.entranceConnector;
            if (entranceLocal == null) continue;

            Vector3 entranceDirLocal = Vector3.at(
                    entranceLocal.getDirection().getX(),
                    entranceLocal.getDirection().getY(),
                    entranceLocal.getDirection().getZ());
            Vector3 exitDir = Vector3.at(exit.getDirection().getX(), exit.getDirection().getY(), exit.getDirection().getZ())
                    .normalize();

            for (int rotation : new int[]{0, 90, 180, 270}) {
                Transform transform = RoomTemplate.rotationTransform(rotation);

                Vector3 rotatedEntranceDir = RoomTemplate.rotateDirection(transform, entranceDirLocal).normalize();
                if (rotatedEntranceDir.dot(exitDir) > -0.99) continue;

                Vector center = entranceLocal.getCenter();
                BlockVector3 localOrigin = BlockVector3.at(
                        (int) Math.floor(center.getX()), (int) Math.floor(center.getY()), (int) Math.floor(center.getZ()));

                Vector exitCenter = exit.getCenter();
                BlockVector3 pasteAt = BlockVector3.at(
                        (int) Math.floor(exitCenter.getX()), (int) Math.floor(exitCenter.getY()), (int) Math.floor(exitCenter.getZ()));

                BoundingBox candidateBox = candidate.computeWorldBounds(transform, localOrigin, pasteAt);
                candidateBox.expand(1, 1, 1);

                boolean collides = existingBoxes.stream().anyMatch(b -> b.overlaps(candidateBox));
                if (collides) continue;

                RoomInstance instance = new RoomInstance(candidate);
                Location pasteLoc = new Location(world, pasteAt.x(), pasteAt.y(), pasteAt.z());
                instance.generate(pasteLoc, rotation);
                return instance;
            }
        }

        exit.block(world);
        return null;
    }

    
    private Vector getRelativeLocation(Vector worldLocation, Vector relativePoint){
        return relativePoint.subtract(worldLocation);
    }

    
    private static class Placement {
        final RoomTemplate template;
        final int rotation;
        final BlockVector3 pasteAt;

        Placement(RoomTemplate template, int rotation, BlockVector3 pasteAt) {
            this.template = template;
            this.rotation = rotation;
            this.pasteAt = pasteAt;
        }
    }

    
    private static Placement findPlacement(Connector exit, List<RoomTemplate> candidatePool,
                                           List<RoomInstance> existingInstances, RoomInstance sourceRoom) {
        // NOTE: entranceConnector.getDirection() is not reliable here — BuildEvents always
        // constructs entrances with a hardcoded (1,0,0) direction, so it doesn't reflect
        // which way an entrance actually faces. Room templates instead follow a fixed
        // convention (entrances face -X locally, pre-rotation), the same convention
        // nextRoom() relies on via RoomTemplate.rotationToFace(). The required rotation
        // is therefore a function of the exit alone, not of any per-candidate data, so
        // it's computed once up front instead of brute-forced per candidate.
        Vector exitDirRaw = exit.getDirection();
        Vector3 exitDir = Vector3.at(exitDirRaw.getX(), 0, exitDirRaw.getZ()).normalize();
        Vector3 requiredEntranceFacing = exitDir.multiply(-1);

        int rotation = RoomTemplate.rotationToFace(requiredEntranceFacing);
        Transform transform = RoomTemplate.rotationTransform(rotation);

        Vector exitCenter = exit.getCenter();
        BlockVector3 pasteAt = BlockVector3.at(
                (int) Math.floor(exitCenter.getX()), (int) Math.floor(exitCenter.getY()), (int) Math.floor(exitCenter.getZ()));

        for (RoomTemplate candidate : candidatePool) {
            Connector entranceLocal = candidate.entranceConnector;
            if (entranceLocal == null) continue;

            // Disqualify dead-end candidates: a single exit that would face backward
            // (after rotation) can never lead anywhere useful, so skip it entirely.
            if (candidate.exitConnectors.size() == 1) {
                Vector dirRaw = candidate.exitConnectors.get(0).getDirection();
                Vector3 exitDirLocal = Vector3.at(dirRaw.getX(), dirRaw.getY(), dirRaw.getZ());
                Vector3 rotatedExitDir = RoomTemplate.rotateDirection(transform, exitDirLocal).normalize();
                if (RoomTemplate.isBackwardDirection(rotatedExitDir)) continue;
            }

            Vector center = entranceLocal.getCenter();
            BlockVector3 localOrigin = BlockVector3.at(
                    (int) Math.floor(center.getX()), (int) Math.floor(center.getY()), (int) Math.floor(center.getZ()));

            BoundingBox candidateBox = candidate.computeWorldBounds(transform, localOrigin, pasteAt);
            candidateBox.expand(1, 1, 1);

            boolean collides = existingInstances.stream()
                    .filter(inst -> inst != sourceRoom)
                    .anyMatch(inst -> inst.boundingBox.overlaps(candidateBox));
            if (collides) continue;

            return new Placement(candidate, rotation, pasteAt);
        }
        return null;
    }

    
    public static RoomInstance generateFromExit(Connector exit, World world,
                                                List<RoomTemplate> candidatePool,
                                                List<RoomInstance> existingInstances,
                                                RoomInstance sourceRoom) {
        Placement placement = findPlacement(exit, candidatePool, existingInstances, sourceRoom);
        if (placement == null) {
            exit.block(world);
            return null;
        }

        RoomInstance instance = new RoomInstance(placement.template);
        Location pasteLoc = new Location(world, placement.pasteAt.x(), placement.pasteAt.y(), placement.pasteAt.z());
        instance.generate(pasteLoc, placement.rotation);
        return instance;
    }

    
    public static boolean canGenerateFrom(Connector exit, List<RoomTemplate> candidatePool,
                                          List<RoomInstance> existingInstances, RoomInstance sourceRoom) {
        Vector dirRaw = exit.getDirection();
        if (RoomTemplate.isBackwardDirection(Vector3.at(dirRaw.getX(), dirRaw.getY(), dirRaw.getZ()))) {
            return false;
        }
        return findPlacement(exit, candidatePool, existingInstances, sourceRoom) != null;
    }
}