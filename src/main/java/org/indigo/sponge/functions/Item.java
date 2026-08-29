package org.indigo.sponge.functions;

import io.papermc.paper.datacomponent.item.FoodProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {

    public static void createWeapon(String id, ItemStack item, String name, String desc, Double value, String subCategory, String abilityId, Integer floorlevel, Double damage, Double attack_speed, Double additionalStat ) {
        String category = "weapon-" + subCategory;
        subCategory = Utils.toProperCase(subCategory);
        ItemMeta meta = item.getItemMeta();

        List<Component> loreList = new ArrayList<>(List.of(Component.text(""), Component.text("")));
        List<Component> loreLineList;
        String loreColor = "";

        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "damage"), PersistentDataType.DOUBLE, damage);
        String damageStr = Utils.translateStats("inc", "damage", damage);
        loreLineList = Utils.translateLore("Damage: " + damageStr, "<gray>", 1);
        loreList.addAll(loreLineList);
        loreLineList.clear();

        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "attack_speed"), PersistentDataType.DOUBLE, attack_speed);
        String atkSpeedStr = Utils.translateStats("inc", "attack_speed", attack_speed);
        loreLineList = Utils.translateLore("Attack Speed: " + atkSpeedStr, "<gray>", 1);
        loreList.addAll(loreLineList);
        loreLineList.clear();

        if (subCategory.equalsIgnoreCase("Light")  || subCategory.equalsIgnoreCase("Medium") || subCategory.equalsIgnoreCase("Heavy"))
        {
            loreList.set(0, MiniMessage.miniMessage().deserialize(" <dark_gray><!i>" + subCategory + " Melee Weapon"));
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><font:lore_icons>a<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "melee"));
            loreColor = "#FCA800";
        }
        if (subCategory.equalsIgnoreCase("Ranged"))
        {
            loreList.set(0, MiniMessage.miniMessage().deserialize(" <dark_gray><!i>" + subCategory + " Ranged Weapon"));
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><font:lore_icons>b<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "ranged"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "maxAmmo"), PersistentDataType.DOUBLE, additionalStat);
            loreColor = "#54FC54";
        }
        if (subCategory.equalsIgnoreCase("Magic"))
        {
            loreList.set(0, MiniMessage.miniMessage().deserialize(" <dark_gray><!i>" + subCategory + " Magic Weapon"));
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><font:lore_icons>c<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "magic"));
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "consume"), PersistentDataType.DOUBLE, additionalStat);
            loreColor = "#7800FF";
        }
        if (id.equalsIgnoreCase("test"))
        {
            meta.setTooltipStyle(new NamespacedKey("minecraft", "kitty"));
        }
        if (Sponge.itemStatDic.containsKey(id))
        {
            HashMap<String,Double> itemStats =  Sponge.itemStatDic.get(id);
            for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
                String stat = entry.getKey();
                Double statValue = entry.getValue();
                String statStr = Utils.translateStats("inc", stat, statValue);
                stat = Utils.toProperCase( stat.replace("_", " "));
                loreLineList = Utils.translateLore(stat + ": " + statStr, "<gray>", 1);
                loreList.addAll(loreLineList);
                loreLineList.clear();
            }
        }

        loreList.add(Component.text(""));
        loreList.add(MiniMessage.miniMessage().deserialize("<!i>   <gradient:dark_gray:" + loreColor + "><st>                <!st>⏴<reset><!i> <" + loreColor + ">ᴅᴇsᴄʀɪᴘᴛɪᴏɴ <gradient:" + loreColor + ":dark_gray>⏵<st>                <!st>"));
        List<Component>  descList = Utils.translateLore(desc, "<gray>", 1);
        loreList.addAll(descList);

        loreList.add(Component.text(""));
        meta.lore(loreList);
        meta.setMaxStackSize(1);
        meta.isUnbreakable();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "id"), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "category"), PersistentDataType.STRING, category);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "value"), PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
    public static void createArmor(String id, ItemStack item, String name, String desc, Double value, String subCategory, Integer floorlevel, Double armor) {
        String category = "armor-" + subCategory;
        subCategory = Utils.toProperCase(subCategory);
        ItemMeta meta = item.getItemMeta();

        List<Component> loreList = new ArrayList<>(List.of(MiniMessage.miniMessage().deserialize(" <dark_gray><!i>" + subCategory + " Armor"), Component.text("")));
        List<Component> loreLineList;
        String loreColor = "";

        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "armor"), PersistentDataType.DOUBLE, armor);
        String armorStr = Utils.translateStats("inc", "armor", armor);
        loreLineList = Utils.translateLore("Armor: " + armorStr, "<gray>", 1);
        loreList.addAll(loreLineList);
        loreLineList.clear();

        if (subCategory.equalsIgnoreCase("Melee"))
        {
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><#FCA800><font:lore_icons>e<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "melee"));
            loreColor = "#FCA800";
        }
        if (subCategory.equalsIgnoreCase("Ranged"))
        {
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><#54FC54><font:lore_icons>e<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "ranged"));
            loreColor = "#54FC54";
        }
        if (subCategory.equalsIgnoreCase("Magic"))
        {
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><#7800FF><font:lore_icons>e<reset> <dark_gray>» <reset><!i>" + name));
            meta.setTooltipStyle(new NamespacedKey("minecraft", "magic"));
            loreColor = "#7800FF";
        }
        if (Sponge.itemStatDic.containsKey(id))
        {
            HashMap<String,Double> itemStats =  Sponge.itemStatDic.get(id);
            for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
                String stat = entry.getKey();
                Double statValue = entry.getValue();
                String statStr = Utils.translateStats("inc", stat, statValue);
                stat = Utils.toProperCase( stat.replace("_", " "));
                loreLineList = Utils.translateLore(stat + ": " + statStr, "<gray>", 1);
                loreList.addAll(loreLineList);
                loreLineList.clear();
            }
        }

        loreList.add(Component.text(""));
        loreList.add(MiniMessage.miniMessage().deserialize("<!i>   <gradient:dark_gray:" + loreColor + "><st>                <!st>⏴<reset><!i> <" + loreColor + ">ᴅᴇsᴄʀɪᴘᴛɪᴏɴ <gradient:" + loreColor + ":dark_gray>⏵<st>                <!st>"));
        List<Component>  descList = Utils.translateLore(desc, "<gray>", 1);
        loreList.addAll(descList);

        loreList.add(Component.text(""));
        meta.lore(loreList);
        meta.setMaxStackSize(1);
        meta.isUnbreakable();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "id"), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "category"), PersistentDataType.STRING, category);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "value"), PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
    public static void createAccessory(String id, ItemStack item, String name, String desc, Double value, Integer floorlevel) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!i><#54FCFC>⭐ <dark_gray>» <reset><!i>" + name));
        String category = "accessory";

        List<Component> loreList = new ArrayList<>(List.of(MiniMessage.miniMessage().deserialize(" <dark_gray><!i>Accessory"), Component.text("")));
        List<Component> loreLineList;

        if (Sponge.itemStatDic.containsKey(id))
        {
            HashMap<String,Double> itemStats =  Sponge.itemStatDic.get(id);
            for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
                String stat = entry.getKey();
                Double statValue = entry.getValue();
                String statStr = Utils.translateStats("inc", stat, statValue);
                stat = Utils.toProperCase( stat.replace("_", " "));
                loreLineList = Utils.translateLore(stat + ": " + statStr, "<gray>", 1);
                loreList.addAll(loreLineList);
                loreLineList.clear();
            }
        }

        loreList.add(Component.text(""));
        loreList.add(MiniMessage.miniMessage().deserialize("<!i>   <gradient:dark_gray:#54FCFC><st>                <!st>⏴<reset><!i> <#54FCFC>ᴅᴇsᴄʀɪᴘᴛɪᴏɴ <gradient:#54FCFC:dark_gray>⏵<st>                <!st>"));
        List<Component>  descList = Utils.translateLore(desc, "<gray>", 1);
        loreList.addAll(descList);

        loreList.add(Component.text(""));
        meta.lore(loreList);
        meta.setTooltipStyle(new NamespacedKey("minecraft", "accessory"));
        meta.setMaxStackSize(1);
        meta.isUnbreakable();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "id"), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "category"), PersistentDataType.STRING, category);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "value"), PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
    public static void createConsumable(String id, ItemStack item, String name, String desc, Double value, String subCategory, Integer floorlevel) {
        List<String> cats = List.of("food", "throwable");
        List<String> icons = List.of("🍖", "☄");
        HashMap<String, String> catsToIcons = new HashMap<>();
        for (int i = 0; i < Math.min(cats.size(), icons.size()); i++) {
            catsToIcons.put(cats.get(i), icons.get(i));
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!i><#FC5454>" + catsToIcons.get(subCategory) + " <dark_gray>» <reset><!i>" + name));
        String category = "consumable-" + subCategory;
        subCategory = Utils.toProperCase(subCategory);

        List<Component> loreList = new ArrayList<>(List.of(MiniMessage.miniMessage().deserialize(" <dark_gray><!i>" + subCategory), Component.text("")));
        List<Component> loreLineList;

        if (Sponge.itemStatDic.containsKey(id)) {
            HashMap<String, Double> itemStats = Sponge.itemStatDic.get(id);
            for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
                String stat = entry.getKey();
                Double statValue = entry.getValue();
                String statStr = Utils.translateStats("inc", stat, statValue);
                stat = Utils.toProperCase(stat.replace("_", " "));
                loreLineList = Utils.translateLore(stat + ": " + statStr, "<gray>", 1);
                loreList.addAll(loreLineList);
                loreLineList.clear();
            }
        }

        loreList.add(Component.text(""));
        loreList.add(MiniMessage.miniMessage().deserialize("<!i>   <gradient:dark_gray:#FC5454><st>                <!st>⏴<reset><!i> <#FC5454>ᴅᴇsᴄʀɪᴘᴛɪᴏɴ <gradient:#FC5454:dark_gray>⏵<st>                <!st>"));
        List<Component> descList = Utils.translateLore(desc, "<gray>", 1);
        loreList.addAll(descList);

        loreList.add(Component.text(""));
        meta.lore(loreList);
        meta.setTooltipStyle(new NamespacedKey("minecraft", "consumable"));
        meta.setMaxStackSize(99);
        meta.isUnbreakable();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        if (category == "consumable-food")
        {

        }
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "id"), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "category"), PersistentDataType.STRING, category);
        meta.getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin, "value"), PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
        if (floorlevel == 0)
        {
            Sponge.floorAllItems.put(id, item);
        }
        Sponge.itemDic.put(id, item);
    }
}
