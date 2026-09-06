package org.indigo.sponge.rooms;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Connector {
    private World world;
    private BoundingBox boundingBox;
    private BlockDisplay display;
    private ConnectorType type;
    private Location corner1;
    private Location corner2;

    public enum ConnectorType{
        ENTRANCE,
        EXIT
    }
    public Connector(Location corner1, Location corner2, ConnectorType type){
        this.world = corner1.getWorld();
        this.corner1 = corner1;
        this.corner2 = corner2;
        boundingBox = new BoundingBox(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
        this.type = type;
    }

    public void setCorner1(Location location){
        this.corner1 = location;
        updateBounds();
    }

    public void setCorner2(Location location){
        this.corner2 = location;
        updateBounds();
    }

    private void updateBounds(){
        boundingBox.resize(corner1.x(),corner1.y(),corner1.z(),corner2.x(),corner2.y(),corner2.z());
    }

    public void spawnDisplayEntity() {
        removeDisplayEntity();
        if (world == null) return;

        Location min = new Location(world,boundingBox.getMinX(),boundingBox.getMinY(),boundingBox.getMinZ());
        Location max = new Location(world,boundingBox.getMaxX(),boundingBox.getMaxY(),boundingBox.getMaxZ());

        BlockDisplay entity = (BlockDisplay) world.spawnEntity(min, EntityType.BLOCK_DISPLAY);
        entity.setBlock(Bukkit.createBlockData(Material.GLASS));
        entity.setGlowing(true);
        if(type == ConnectorType.ENTRANCE) {
            entity.setGlowColorOverride(Color.ORANGE);
        } else {
            entity.setGlowColorOverride(Color.BLUE);
        }

        float sizeX = (float) (max.getBlockX() - min.getBlockX() + 1);
        float sizeY = (float) (max.getBlockY() - min.getBlockY() + 1);
        float sizeZ = (float) (max.getBlockZ() - min.getBlockZ() + 1);

        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(sizeX, sizeY, sizeZ),
                new Quaternionf()
        ));

        display = entity;
    }

    public void removeDisplayEntity() {
        if (display != null && display.isValid()) {
            display.remove();
        }
        display = null;
    }
}
