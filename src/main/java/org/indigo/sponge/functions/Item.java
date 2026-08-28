package org.indigo.sponge.functions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

import java.awt.*;

public class Item {

    public static void createWeapon(String id, ItemStack item, String name, String desc, Double value, String subCategory, String abilityId, Integer floorlevel, Double dmg, Double atkSpeed, Double additionalStat ) {
        String category = "weapon-" + subCategory;
        subCategory = subCategory.toUpperCase();
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "dmg"), PersistentDataType.DOUBLE, dmg);
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "atkSpeed"), PersistentDataType.DOUBLE, atkSpeed);
        if (subCategory.equalsIgnoreCase("Light")  || subCategory.equalsIgnoreCase("Medium") || subCategory.equalsIgnoreCase("Heavy")) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>a<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "melee"));
            item.setItemMeta(meta);
        }
        if (subCategory.equalsIgnoreCase("Ranged"))
        {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>b<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "ranged"));
            item.setItemMeta(meta);
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "maxAmmo"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (subCategory.equalsIgnoreCase("Magic"))
        {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>c<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "magic"));
            item.setItemMeta(meta);
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "consume"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (id.equalsIgnoreCase("test"))
        {
            ItemMeta meta = item.getItemMeta();
            meta.setTooltipStyle(new NamespacedKey("minecraft", "kitty"));
            item.setItemMeta(meta);
        }

        if (Sponge.itemStatDic.containsKey(id))
        {

        }

        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
}
