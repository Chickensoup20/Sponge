package org.indigo.sponge.item.bahavior;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UseContext {
    private final Player user;
    private final Location clickedLocation;
    private final ItemStack item;

    public UseContext(Player user, Location clickedLocation, ItemStack item) {
        this.user = user;
        this.clickedLocation = clickedLocation;
        this.item = item;
    }

    public Player getUser() {
        return user;
    }

    public ItemStack getItem() {
        return item;
    }

    public Location getClickedLocation() {
        return clickedLocation;
    }
}
