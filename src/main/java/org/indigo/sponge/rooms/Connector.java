package org.indigo.sponge.rooms;

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
    private World world;
    public BoundingBox boundingBox;
    private BlockDisplay display;
    private ConnectorType type;
    private Location corner1;
    private Location corner2;
    private Vector direction;

    public enum ConnectorType{
        ENTRANCE,
        EXIT
    }
    public Connector(Location corner1, Location corner2, ConnectorType type,Vector direction){
        this.world = corner1.getWorld();
        this.corner1 = corner1;
        this.corner2 = corner2;
        boundingBox = new BoundingBox(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
        this.type = type;
        this.direction = direction;
    }

    public void setCorner1(Location location){
        this.corner1 = location;
        updateBounds();
    }

    public void setCorner2(Location location){
        this.corner2 = location;
        updateBounds();
    }

    public void setDirection(Vector direction){
        this.direction = direction;
    }

    private void updateBounds(){
        boundingBox.resize(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
        boundingBox.resize(boundingBox.getMinX(), Math.floor(boundingBox.getMinY()), boundingBox.getMinZ(), boundingBox.getMaxX()+1, boundingBox.getMaxY()+1, boundingBox.getMaxZ()+1);
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
                new Vector3f(sizeX, sizeY, sizeZ),
                new Quaternionf()
        ));

        entity.setBrightness(new Display.Brightness(15,15));

        display = entity;
    }

    public void removeDisplayEntity() {
        if (display != null && display.isValid()) {
            display.remove();
        }
        display = null;
    }
}
