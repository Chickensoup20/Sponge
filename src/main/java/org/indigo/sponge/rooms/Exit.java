package org.indigo.sponge.rooms;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;

public class Exit extends Portal {

    public Exit() {
    }

    public Exit(Location corner1, Location corner2) {
        super(corner1, corner2);
    }

    @Override
    protected Material getMaterial() {
        return Material.LIGHT_BLUE_STAINED_GLASS;
    }

    @Override
    protected Color getGlowColor() {
        return Color.fromRGB(0x87CEEB); // sky blue
    }
}
