package org.indigo.sponge.functions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

import java.awt.*;

public class Item extends Sponge {

    public static void CreateWeapon(String id, ItemStack item, String name, String desc, Double value, String subCategory, String abilityId, Integer floorlevel, Double dmg, Double atkSpeed, Double additionalStat ) {
        String category = "weapon-" + subCategory;
        subCategory = subCategory.toUpperCase();
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(plugin, "dmg"), PersistentDataType.DOUBLE, dmg);
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(plugin, "atkSpeed"), PersistentDataType.DOUBLE, atkSpeed);
        if (subCategory == "Light" || subCategory == "Medium" || subCategory == "Heavy") {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>a<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey(plugin, "melee"));
            item.setItemMeta(meta);

        }
        if (subCategory == "Ranged")
        {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>b<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey(plugin, "ranged"));
            item.setItemMeta(meta);
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(plugin, "maxAmmo"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (subCategory == "Magic")
        {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>c<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey(plugin, "magic"));
            item.setItemMeta(meta);
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(plugin, "consume"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (id == "test")
        {
            ItemMeta meta = item.getItemMeta();
            meta.setTooltipStyle(new NamespacedKey(plugin, "kitty"));
            item.setItemMeta(meta);
        }
        itemDic.put(id, item);
    }
}
