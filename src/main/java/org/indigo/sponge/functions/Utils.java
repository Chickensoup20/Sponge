package org.indigo.sponge.functions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Sponge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static ItemStack makeMenuItem(String type, ItemStack item, Component name, Component desc, String descColor)
    {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(Sponge.plugin,"type"), PersistentDataType.STRING,type);
        item.setItemMeta(meta);

        return item;
    }

    public static void createStatDic(String id, String type, List<String> stats, List<Double> values)
    {
        HashMap<String, Double> allStats = new HashMap<>();
        int size = Math.min(stats.size(), values.size());
        for (int i = 0; i < size; i++) {
            allStats.put(stats.get(i), values.get(i));
        }

        Sponge.itemStatDic.put(id, allStats);
    }

    public static String translateStats(String type, String stat, String value)
    {
        List<String> stats = List.of("dmg", "health", "armor", "speed", "resistance", "evasion", "ether", "atk_speed", "max_ammo", "range", "crit_chance", "heal", "regeneration", "incEther", "money", "fire", "poison", "electric");
        List<String> colors = List.of("<#FF2828>[V] #14", "<#AA3131>[V] #04", "<#C9D8F9>[V]% #05", "<#5353F9>[V] #06", "<#5ECBE1>[V]% #07", "<#FFFFFF>[V]% #08", "<#BB80ED>[V] #09", "<#26de69>[V] #10", "<#FF8132>[V] #11", "<#FFEE6D>[V] #16", "<#FFD5A5>[V]% #20", "<#70FF5B>[V] #15", "<#70FF5B>[V]/s #15", "<#BB80ED>[V] #09", "<#FFC635>[V] #19", "<#FF6E14>[V]/s #17", "<#4B9940>[V]/s #18", "<#D0FFEA>[V]/s #21");
        HashMap<String, String> allStats = new HashMap<>();
        int size = Math.min(stats.size(), colors.size());
        for (int i = 0; i < size; i++) {
            allStats.put(stats.get(i), colors.get(i));
        }

        String finalColor = allStats.get(stat);
        if (type.equalsIgnoreCase("inc"))
        {
            finalColor = finalColor.replace("[V]", "+" + value);
        }
        else if (type.equalsIgnoreCase("dec"))
        {
            finalColor = finalColor.replace("[V]", "-" + value);
        }
        else
        {
            finalColor = finalColor.replace("[V]", value);
        }
        return finalColor;
    }

}
