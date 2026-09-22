package org.indigo.sponge.registries;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.Colors;
import org.indigo.sponge.functions.Utils;

import java.util.HashMap;

public class Items {
    public static HashMap<String, ItemStack> itemDic = new HashMap<>(); // Contains all items in the game
    public static HashMap<String, ItemStack> floorAllItems = new HashMap<>(); // Contains only items for all floors

    public static HashMap<String, HashMap<String, Double>> itemStatDic = new HashMap<>(); // Contains additional item stats
    public static ItemStack entranceWand = Utils.createItem(Material.BLAZE_ROD, Colors.toMM(Colors.ORANGE_LIGHT) + "Entrance Wand","entrancewand");
    public static ItemStack exitWand = Utils.createItem(Material.BREEZE_ROD,Colors.toMM(Colors.SKY_LIGHT) + "Exit Wand","exitwand");
}
