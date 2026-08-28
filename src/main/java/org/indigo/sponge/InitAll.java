package org.indigo.sponge;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.functions.Item;

import java.awt.*;

public class InitAll {

    public static void makeWeapons()
    {
        Item.createWeapon("test", new ItemStack(Material.STICK, 1), "Test Stick", "This is a test stick wowie", 999.0, "Light", "none", -1, 999.0, 1.0, 0.0);




        Item.createWeapon("trainingSword", new ItemStack(Material.IRON_SHOVEL,1),"Training Sword", "Every powerful swordsman has wielded one of these at some point.", 15.0, "medium", "none", -1, 2.2, 1.0, 0.0);
    }

    public static void makeArmors()
    {
    }

    public static void makeAccessories()
    {
    }

    public static void makeConsumables()
    {
    }
}
