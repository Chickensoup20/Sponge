package org.indigo.sponge.functions;

import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {

    public static void createWeapon(String id, ItemStack item, String name, String desc, Double value, String subCategory, String abilityId, Integer floorlevel, Double dmg, Double atkSpeed, Double additionalStat ) {
        String category = "weapon-" + subCategory;
        subCategory = subCategory.toUpperCase();
        ItemMeta meta = item.getItemMeta();

        List<Component> loreList = new ArrayList<>();
        List<Component> loreLineList;

        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "dmg"), PersistentDataType.DOUBLE, dmg);
        String dmgStr = Utils.translateStats("set", "dmg", dmg);
        loreLineList = Utils.translateLore("Damage:" + dmgStr, "<gray>", 1);
        loreList.addAll(loreLineList);
        loreLineList.clear();

        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "atkSpeed"), PersistentDataType.DOUBLE, atkSpeed);
        String atkSpeedStr = Utils.translateStats("set", "atk_speed", atkSpeed);
        loreLineList = Utils.translateLore("Atk. Speed:" + atkSpeedStr, "<gray>", 1);
        loreList.addAll(loreLineList);
        loreLineList.clear();

        if (subCategory.equalsIgnoreCase("Light")  || subCategory.equalsIgnoreCase("Medium") || subCategory.equalsIgnoreCase("Heavy")) {

            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>a<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "melee"));
        }
        if (subCategory.equalsIgnoreCase("Ranged"))
        {
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>b<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "ranged"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "maxAmmo"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (subCategory.equalsIgnoreCase("Magic"))
        {
            meta.displayName(MiniMessage.miniMessage().deserialize("<font:lore_icons>c<reset> <dark_gray>» <reset>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "magic"));
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "consume"), PersistentDataType.DOUBLE, additionalStat);
        }
        if (id.equalsIgnoreCase("test"))
        {
            meta.setTooltipStyle(new NamespacedKey("minecraft", "kitty"));
        }

        if (Sponge.itemStatDic.containsKey(id))
        {
            loreList.add(Component.newline());
            HashMap<String,Double> itemStats =  Sponge.itemStatDic.get(id);
            for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
                String stat = entry.getKey();
                Double statValue = entry.getValue();
                String statStr = Utils.translateStats("inc", stat, statValue);
                stat = stat.replace("_", " ").toUpperCase();
                loreLineList = Utils.translateLore(stat + ": " + statValue, "<gray>", 1);
                loreList.addAll(loreLineList);
                loreLineList.clear();
            }
        }
        loreList.add(Component.newline());
        meta.lore(loreList);
        meta.setMaxStackSize(1);
        meta.isUnbreakable();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "id"), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "category"), PersistentDataType.STRING, category);
        item.setItemMeta(meta);
        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
}
