package org.indigo.sponge.rooms;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;

public class Entrance extends Portal {

    public Entrance() {
    }

    public Entrance(Location corner1, Location corner2) {
        super(corner1, corner2);
    }

    @Override
    protected Material getMaterial() {
        return Material.ORANGE_STAINED_GLASS;
    }

    @Override
    protected Color getGlowColor() {
        return Color.ORANGE;
    }
}
