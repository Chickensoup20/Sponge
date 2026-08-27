package org.indigo.sponge;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.functions.Item;

import java.awt.*;

public class InitAll extends Sponge {

    public static void MakeWeapons()
    {
        Item.CreateWeapon("test", new ItemStack(Material.STICK, 1), "Test Stick", "This is a test stick wowie", 999.0, "Light", "none", -1, 999.0, 1.0, 0.0);
    }

    public static void MakeArmors()
    {
    }

    public static void MakeAccessories()
    {
    }

    public static void MakeConsumables()
    {
    }
}
