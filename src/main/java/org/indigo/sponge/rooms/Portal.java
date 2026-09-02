package org.indigo.sponge.rooms;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public abstract class Portal {

    protected UUID id;
    protected String worldName;
    protected double x1, y1, z1;
    protected double x2, y2, z2;

    private transient BlockDisplay displayEntity;

    protected Portal() {
    }

    protected Portal(Location corner1, Location corner2) {
        this.id = UUID.randomUUID();
        World world = corner1.getWorld();
        this.worldName = world != null ? world.getName() : null;
        this.x1 = Math.floor(corner1.getX());
        this.y1 = Math.floor(corner1.getY());
        this.z1 = Math.floor(corner1.getZ());
        this.x2 = Math.floor(corner2.getX());
        this.y2 = Math.floor(corner2.getY());
        this.z2 = Math.floor(corner2.getZ());
    }

    public UUID getId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        return id;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getMin() {
        return new Location(getWorld(), Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
    }

    public Location getMax() {
        return new Location(getWorld(), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public boolean contains(Location loc) {
        World world = getWorld();
        if (world == null || loc.getWorld() == null || !world.equals(loc.getWorld())) {
            return false;
        }

        Location min = getMin();
        Location max = getMax();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }

    public BlockDisplay getDisplayEntity() {
        return displayEntity;
    }

    public void setDisplayEntity(BlockDisplay displayEntity) {
        this.displayEntity = displayEntity;
    }

    public void removeDisplayEntity() {
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.remove();
        }
        displayEntity = null;
    }

    protected abstract Material getMaterial();

    protected abstract Color getGlowColor();


    public void spawnDisplayEntity() {
        removeDisplayEntity();

        World world = getWorld();
        if (world == null) return;

        Location min = getMin();
        Location max = getMax();

        BlockDisplay entity = (BlockDisplay) world.spawnEntity(min, EntityType.BLOCK_DISPLAY);
        entity.setBlock(Bukkit.createBlockData(getMaterial()));
        entity.setGlowing(true);
        entity.setGlowColorOverride(getGlowColor());

        float sizeX = (float) (max.getBlockX() - min.getBlockX() + 1);
        float sizeY = (float) (max.getBlockY() - min.getBlockY() + 1);
        float sizeZ = (float) (max.getBlockZ() - min.getBlockZ() + 1);

        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(sizeX, sizeY, sizeZ),
                new Quaternionf()
        ));

        this.displayEntity = entity;
    }
}
