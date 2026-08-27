package org.indigo.sponge.functions;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

public class Utils extends Sponge {

    public static ItemStack makeMenuItem(String type, ItemStack item, Component name, Component desc, String descColor)
    {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(plugin,"type"), PersistentDataType.STRING,type);
        item.setItemMeta(meta);

        return item;
    }
}
