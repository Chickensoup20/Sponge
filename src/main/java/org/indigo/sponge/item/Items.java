package org.indigo.sponge.item;

import org.bukkit.Material;
import org.indigo.sponge.registries.ItemRegistry;
import org.indigo.sponge.stats.Stat;

public class Items {
    public static void createItems(){
        FloorItem trainingSword = FloorItem.builder("trainingSword")
                .material(Material.IRON_SWORD)
                .rarity(ItemRarity.COMMON)
                .description("The worst sword lmao")
                .displayName("Training Sword")
                .stat(Stat.DAMAGE,12.0)
                .build();
        ItemRegistry.register(trainingSword);
    }

}
