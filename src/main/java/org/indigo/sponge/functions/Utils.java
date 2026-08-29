package org.indigo.sponge.functions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.indigo.sponge.Colors;
import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.indigo.sponge.Sponge.mm;

public class Utils {

    public static String toProperCase(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        return result.toString().trim();
    }

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

    public static String translateStats(String type, String stat, Object value)
    {
        List<String> stats = List.of("damage", "health", "armor", "speed", "resistance", "evasion", "ether", "attack_speed", "max_ammo", "range", "crit_chance", "heal", "regeneration", "incEther", "money", "fire", "poison", "electric");
        List<String> colors = List.of("<#FF2828>[V] #14", "<#AA3131>[V] #04", "<#C9D8F9>[V%] #05", "<#5353F9>[V] #06", "<#5ECBE1>[V%] #07", "<#FFFFFF>[V%] #08", "<#BB80ED>[V] #09", "<#26de69>[V] #10", "<#FF8132>[V] #11", "<#FFEE6D>[V] #16", "<#FFD5A5>[V%] #20", "<#70FF5B>[V] #15", "<#70FF5B>[V/s] #15", "<#BB80ED>[V] #09", "<#FFC635>[V] #19", "<#FF6E14>[V/s] #17", "<#4B9940>[V/s] #18", "<#D0FFEA>[V/s] #21");
        HashMap<String, String> allStats = new HashMap<>();
        int size = Math.min(stats.size(), colors.size());
        for (int i = 0; i < size; i++) {
            allStats.put(stats.get(i), colors.get(i));
        }

        String valueStr =  value.toString().replaceAll("\\.0(?!\\d)", "");

        String finalColor = allStats.get(stat);
        String target = "[V]";
        String result = " ";
        if (finalColor.contains("%"))
        {
            target = "[V%]";
            result = "% ";
        }
        else if (finalColor.contains("/s"))
        {
            target = "[V/s]";
            result = "/s ";
        }
        if (type.equalsIgnoreCase("inc"))
        {
            finalColor = finalColor.replace(target, "+" + valueStr + result);
        }
        else if (type.equalsIgnoreCase("dec"))
        {
            finalColor = finalColor.replace(target, "-" + valueStr + result);
        }
        else
        {
            finalColor = finalColor.replace(target, valueStr + result);
        }
        return finalColor;
    }
    public static List<Component> translateLore(String lore, String txtColor, Integer offset)
    {
        List<String> symbols = List.of("<font:lore_icons>a", "<font:lore_icons>b", "<font:lore_icons>c", "<font:lore_icons>d", "<font:lore_icons>e", "<font:lore_icons>f", "<font:lore_icons>g", "<font:lore_icons>h", "<font:lore_icons>i", "<font:lore_icons>j", "<font:lore_icons>k", "<font:lore_icons>l", "<font:lore_icons>m", "<font:lore_icons>n", "<font:lore_icons>o", "<font:lore_icons>p", "<font:lore_icons>q", "<font:lore_icons>r", "<font:lore_icons>s", "<font:lore_icons>t", "<font:lore_icons>u");
        String[] loreList = lore.split("");

        String newLore = "";
        int ind = 0;
        for (String currChar:loreList)
        {
            ind++;
            if (ind >= 40)
            {
                if (currChar.equalsIgnoreCase("\\"))
                {
                    ind = 0;
                }
                if (currChar.equalsIgnoreCase(" "))
                {
                    currChar = "\\";
                    ind = 0;
                }
            }
            newLore = newLore + currChar;
        }
        String[] newLoreList = newLore.split("\\\\");
        List<Component> retVar = new ArrayList<>();
        for (String line:newLoreList)
        {
            String[] lineList = line.split(" ");
            ind = 1;
            String finalLine = "<!i>";
            for (String word:lineList)
            {
                if (word.startsWith("#")) {
                    int hashInd = word.indexOf("#", 0);
                    int startInd = hashInd + 1;
                    int endInd = hashInd + 3;
                    word = word.substring(startInd, endInd);
                    int symbolInd = Integer.parseInt(word) - 1;
                    String symbol = symbols.get(symbolInd);
                    finalLine = finalLine + "<white><!i>" + symbol + "<reset>";
                }
                else
                {
                    finalLine = finalLine + " " + txtColor + word;
                }
                ind++;
            }
            for (int i = 0; i < offset; i++) {
                finalLine = " " + finalLine;
            }
            retVar.add(MiniMessage.miniMessage().deserialize(finalLine));
        }
        return retVar;
    }
    public static void sendSystemMessage(Player player, String string){
        player.sendMessage(mm.deserialize(Colors.spongeLogo + Colors.toMM(Colors.GOLD_LIGHT) + " " + string));
    }
}
