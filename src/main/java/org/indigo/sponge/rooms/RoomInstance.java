package org.indigo.sponge.rooms;

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

    /**
     * Creates an unplaced room instance for the given template. Call {@link #generate}
     * to actually paste it into the world.
     * @param roomTemplate the template this instance is based on
     */
    public RoomInstance(RoomTemplate roomTemplate){
        this.roomTemplate = roomTemplate;
    }

    /**
     * Pastes this instance's template schematic into the world at the given
     * location and rotation, then computes its world-space bounding box and
     * transforms its entrance/exit connectors into world space.
     * @param pasteLoc the world location to paste the schematic at
     * @param rotation the yaw rotation (in degrees) to paste with
     */
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

    /**
     * Searches a pool of candidate room templates for one whose entrance can be
     * rotated to face the given exit without its resulting bounding box overlapping
     * any existing room. Pastes and returns the first fit found, or seals the exit
     * and returns null if nothing fits.
     * @param exit the exit connector to generate a new room from
     * @param world the world to paste into
     * @param candidatePool the room templates to try
     * @param existingBoxes bounding boxes of rooms already placed, to check against
     * @return the newly generated room instance, or null if the exit was sealed
     */
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

    /**
     * Computes the position of relativePoint relative to worldLocation.
     * @param worldLocation the reference point
     * @param relativePoint the point to express relative to worldLocation
     * @return the relative offset vector
     */
    private Vector getRelativeLocation(Vector worldLocation, Vector relativePoint){
        return relativePoint.subtract(worldLocation);
    }
}