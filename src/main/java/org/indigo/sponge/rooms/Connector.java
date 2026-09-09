package org.indigo.sponge.rooms;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.indigo.sponge.Colors;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Connector {
    private transient World world;
    public transient BoundingBox boundingBox;
    private transient BlockDisplay display;
    private ConnectorType type;
    private double x1, y1, z1, x2, y2, z2;
    private Vector direction;
    private boolean blocked = false;

    public enum ConnectorType{
        ENTRANCE,
        EXIT
    }

    /**
     * Creates a connector spanning the two given corners, with a facing direction.
     * @param corner1 first corner of the connector's bounding region
     * @param corner2 second corner of the connector's bounding region
     * @param type whether this connector is an ENTRANCE or an EXIT
     * @param direction the direction this connector faces
     */
    public Connector(Location corner1, Location corner2, ConnectorType type, Vector direction){
        this.world = corner1.getWorld();
        this.x1 = corner1.getX(); this.y1 = corner1.getY(); this.z1 = corner1.getZ();
        this.x2 = corner2.getX(); this.y2 = corner2.getY(); this.z2 = corner2.getZ();
        boundingBox = new BoundingBox(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
        this.type = type;
        this.direction = direction;
    }

    /**
     * No-arg constructor used internally by {@link #transformed} to build a
     * rotated/translated copy of an existing connector.
     */
    private Connector() {}

    /**
     * Updates the first corner of this connector and recalculates its bounding box.
     * @param l the new location for corner 1
     */
    public void setCorner1(Location l){
        x1 = l.getX(); y1 = l.getY(); z1 = l.getZ();
        updateBounds();
    }

    /**
     * Updates the second corner of this connector and recalculates its bounding box.
     * @param l the new location for corner 2
     */
    public void setCorner2(Location l){
        x2 = l.getX(); y2 = l.getY(); z2 = l.getZ();
        updateBounds();
    }

    /**
     * Sets the direction this connector faces.
     * @param direction the new facing direction
     */
    public void setDirection(Vector direction){
        this.direction = direction;
    }

    /**
     * Returns the direction this connector faces.
     */
    public Vector getDirection(){
        return direction;
    }

    /**
     * Returns whether this connector is an ENTRANCE or an EXIT.
     */
    public ConnectorType getType(){
        return type;
    }

    /**
     * Returns whether this connector has been sealed off (blocked from further generation).
     */
    public boolean isBlocked(){
        return blocked;
    }

    /**
     * Returns the world-space center point of this connector's bounding box.
     */
    public Vector getCenter(){
        return boundingBox.getCenter();
    }

    /**
     * Recalculates the bounding box from the current corner coordinates, expanding
     * it by one block on the max side so it fully encloses whole blocks.
     */
    private void updateBounds(){
        boundingBox = new BoundingBox(x1, y1, z1, x2, y2, z2);
        boundingBox.resize(
                boundingBox.getMinX(), Math.floor(boundingBox.getMinY()), boundingBox.getMinZ(),
                boundingBox.getMaxX() + 1, boundingBox.getMaxY() + 1, boundingBox.getMaxZ() + 1
        );
    }

    /**
     * Produces a new Connector with this one's local coordinates and direction
     * rotated and translated by the given transform, matching a room paste operation.
     * @param transform the rotation transform applied to the room during paste
     * @param origin the local origin the room was pasted relative to
     * @param paste the world-space location the room was pasted at
     * @param newWorld the world the resulting connector should belong to
     * @return a new Connector positioned and oriented in world space
     */
    public Connector transformed(Transform transform, BlockVector3 origin, BlockVector3 paste, World newWorld) {
        Vector3 originV = origin.toVector3();
        Vector3 pasteV = paste.toVector3();

        Vector3 c1 = transform.apply(Vector3.at(x1, y1, z1).subtract(originV)).add(pasteV);
        Vector3 c2 = transform.apply(Vector3.at(x2, y2, z2).subtract(originV)).add(pasteV);

        Vector3 zero = transform.apply(Vector3.ZERO);
        Vector3 dir = transform.apply(Vector3.at(direction.getX(), direction.getY(), direction.getZ()))
                .subtract(zero);

        Connector result = new Connector();
        result.world = newWorld;
        result.type = this.type;
        result.direction = new Vector(dir.x(), dir.y(), dir.z()).normalize();
        result.x1 = c1.x(); result.y1 = c1.y(); result.z1 = c1.z();
        result.x2 = c2.x(); result.y2 = c2.y(); result.z2 = c2.z();
        result.updateBounds();

        return result;
    }

    /**
     * Reassigns this connector to a world and recalculates its bounding box,
     * used after loading a room template from disk.
     * @param world the world this connector now belongs to
     */
    public void rebuild(World world) {
        this.world = world;
        updateBounds();
    }

    /**
     * Spawns (or respawns) the glass block-display entity marking this connector
     * in the world, colored by connector type.
     */
    public void spawnDisplayEntity() {
        removeDisplayEntity();
        if (world == null) return;

        Location min = new Location(world,boundingBox.getMinX()-0.001,boundingBox.getMinY()-0.001,boundingBox.getMinZ()-0.001);
        Location max = new Location(world,boundingBox.getMaxX()-0.001,boundingBox.getMaxY()-0.001,boundingBox.getMaxZ()-0.001);

        BlockDisplay entity = (BlockDisplay) world.spawnEntity(min, EntityType.BLOCK_DISPLAY);
        entity.setBlock(Bukkit.createBlockData(Material.GLASS));
        entity.setGlowing(true);
        if(type == ConnectorType.ENTRANCE) {
            entity.setGlowColorOverride(Color.ORANGE);
        } else {
            entity.setGlowColorOverride(Color.fromRGB(Colors.SKY.red(),Colors.SKY.green(),Colors.SKY.blue()));
        }

        float sizeX = (float) (max.getBlockX() - min.getBlockX());
        float sizeY = (float) (max.getBlockY() - min.getBlockY());
        float sizeZ = (float) (max.getBlockZ() - min.getBlockZ());

        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f((float) (sizeX+0.002), (float) (sizeY+0.002), (float) (sizeZ+0.002)),
                new Quaternionf()
        ));

        entity.setBrightness(new Display.Brightness(15,15));

        display = entity;
    }

    /**
     * Removes this connector's display entity from the world, if present.
     */
    public void removeDisplayEntity() {
        if (display != null && display.isValid()) {
            display.remove();
        }
        display = null;
    }

    /**
     * Permanently seals this connector: marks it blocked, removes its display
     * entity, and fills its bounding box with solid blocks.
     * @param world the world to place the sealing blocks in
     */
    public void block(World world){
        this.blocked = true;
        removeDisplayEntity();
        sealWithBlocks(world);
    }

    /**
     * Fills this connector's entire bounding box with stone blocks.
     * @param world the world to place the blocks in
     */
    private void sealWithBlocks(World world){
        for (int x = (int) Math.floor(boundingBox.getMinX()); x < boundingBox.getMaxX(); x++)
            for (int y = (int) Math.floor(boundingBox.getMinY()); y < boundingBox.getMaxY(); y++)
                for (int z = (int) Math.floor(boundingBox.getMinZ()); z < boundingBox.getMaxZ(); z++)
                    new Location(world, x, y, z).getBlock().setType(Material.STONE);
    }
}