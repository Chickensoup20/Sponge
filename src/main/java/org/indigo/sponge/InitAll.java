package org.indigo.sponge;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.indigo.sponge.functions.Item;
import org.indigo.sponge.functions.Utils;

import java.util.List;

public class InitAll {

    public static void makeWeapons()
    {
        Item.createWeapon("test", new ItemStack(Material.STICK, 1), "Test Stick", "This is a test stick wowie", 999.0, "Light", "none", -1, 999.0, 1.0, 0.0);
        Utils.createStatDic("trainingSword","item", List.of("speed"), List.of(1.0));
        Item.createWeapon("trainingSword", new ItemStack(Material.IRON_SHOVEL,1),"Training Sword", "Every powerful swordsman has wielded one of these at some point.", 15.0, "medium", "none", -1, 2.2, 1.0, 0.0);
    }

    public static void makeArmors()
    {
        Item.createArmor("leatherHelmet", new ItemStack(Material.LEATHER_HELMET, 1), "Leather Helmet", "A leather helmet you found on the ground somewhere, very dusty but gives just enough protection to save you from serious head injuries.", 20.0, "melee", -1, 2.0);
    }

    public static void makeAccessories()
    {
        Utils.createStatDic("razorBlade", "inc", List.of("dmg", "crit_chance"), List.of(1.5, 4.0));
        Item.createAccessory("razorBlade", new ItemStack(Material.LIGHT_BLUE_DYE, 1), "Razor Blade", "Very sharp, gives crit chance", 10.0, 0);
    }

    public static void makeConsumables()
    {
        Utils.createStatDic("chickenLeg", "inc", List.of("heal"), List.of(25.0));
        Item.createConsumable("chickenLeg", new ItemStack(Material.COOKED_CHICKEN, 1), "Chicken Leg", "ChickenDev now only has 1", 5.0, "food", 0);
    }
}
