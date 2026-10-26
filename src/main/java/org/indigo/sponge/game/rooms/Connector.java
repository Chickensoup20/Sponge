package org.indigo.sponge.game.rooms;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
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

    public Connector(Location corner1, Location corner2, ConnectorType type, Vector direction){
        this.world = corner1.getWorld();
        this.x1 = corner1.getX(); this.y1 = corner1.getY(); this.z1 = corner1.getZ();
        this.x2 = corner2.getX(); this.y2 = corner2.getY(); this.z2 = corner2.getZ();
        boundingBox = new BoundingBox(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
        this.type = type;
        this.direction = direction;
    }

    private Connector() {}

    public void setCorner1(Location l){
        x1 = l.getX(); y1 = l.getY(); z1 = l.getZ();
        updateBounds();
    }

    public void setCorner2(Location l){
        x2 = l.getX(); y2 = l.getY(); z2 = l.getZ();
        updateBounds();
    }

    
    public void setDirection(Vector direction){
        this.direction = direction;
    }

    
    public Vector getDirection(){
        return direction;
    }

    
    public ConnectorType getType(){
        return type;
    }

    
    public boolean isBlocked(){
        return blocked;
    }

    
    public Vector getCenter(){
        return boundingBox.getCenter();
    }

    
    private void updateBounds(){
        boundingBox = new BoundingBox(x1, y1, z1, x2, y2, z2);
        boundingBox.resize(
                boundingBox.getMinX(), Math.floor(boundingBox.getMinY()), boundingBox.getMinZ(),
                boundingBox.getMaxX() + 1, boundingBox.getMaxY() + 1, boundingBox.getMaxZ() + 1
        );
    }

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

    public void rebuild(World world) {
        this.world = world;
        updateBounds();
    }

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
        entity.setPersistent(false);

        display = entity;
    }

    public void removeDisplayEntity() {
        if (display != null && display.isValid()) {
            display.remove();
        }
        display = null;
    }

    public void block(World world){
        this.blocked = true;
        removeDisplayEntity();
        sealWithBlocks(world);
    }

    private void sealWithBlocks(World world){
        for (int x = (int) Math.floor(boundingBox.getMinX()); x < boundingBox.getMaxX(); x++)
            for (int y = (int) Math.floor(boundingBox.getMinY()); y < boundingBox.getMaxY(); y++)
                for (int z = (int) Math.floor(boundingBox.getMinZ()); z < boundingBox.getMaxZ(); z++)
                    new Location(world, x, y, z).getBlock().setType(Material.STONE);
    }
}