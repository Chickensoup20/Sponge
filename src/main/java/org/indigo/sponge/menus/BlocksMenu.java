package org.indigo.sponge.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.indigo.sponge.Sponge;
import org.indigo.sponge.block.CustomBlock;
import org.jetbrains.annotations.NotNull;

public class BlocksMenu implements InventoryHolder {
    private final Inventory inventory;

    public BlocksMenu(Plugin plugin) {
        // Create an Inventory with 9 slots, `this` here is our InventoryHolder.
        this.inventory = plugin.getServer().createInventory(this, 9);
        for(CustomBlock block : CustomBlock.customBlocks){
            inventory.addItem(block.getItem());
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
