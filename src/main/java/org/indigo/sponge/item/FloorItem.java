package org.indigo.sponge.item;

import org.bukkit.Material;
import org.indigo.sponge.item.bahavior.ItemBehavior;
import org.indigo.sponge.stats.Stat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FloorItem {
    private final String id;
    private final Material material;
    private final Map<Stat, Double> stats;
    private final List<ItemBehavior> behaviors;
    private final String description;
    private final ItemRarity rarity;
    private final ItemType type;
    private final ItemSubCategory subCategory;
    private final double value;
    private final int floorLevel;
    private final String displayName;

    private FloorItem(Builder b) {
        this.id = b.id;
        this.material = b.material;
        this.stats = b.stats;
        this.behaviors = b.behaviors;
        this.description = b.description;
        this.rarity = b.rarity;
        this.type = b.type;
        this.subCategory = b.subCategory;
        this.value = b.value;
        this.floorLevel = b.floorLevel;
        this.displayName = b.displayName;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public double getValue() {
        return value;
    }

    public ItemSubCategory getSubCategory() {
        return subCategory;
    }

    public ItemType getType() {
        return type;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }

    public List<ItemBehavior> getBehaviors() {
        return behaviors;
    }

    public Map<Stat, Double> getStats() {
        return stats;
    }

    public Material getMaterial() {
        return material;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static final class Builder {
        private final String id;
        private final Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        private final List<ItemBehavior> behaviors = new ArrayList<>();
        private Material material = Material.STICK;

        private String description = "";
        private ItemRarity rarity = ItemRarity.COMMON;
        private ItemType type = ItemType.WEAPON;
        private ItemSubCategory subCategory = ItemSubCategory.LIGHT;
        private double value = 0.0;
        private int floorLevel = -1;
        private String displayName = "";

        private Builder(String id) {
            this.id = id;
        }

        public Builder behavior(ItemBehavior behavior){
            this.behaviors.add(behavior);
            return this;
        }

        public Builder stat(Stat stat, Double value){
            this.stats.put(stat,value);
            return this;
        }

        public Builder displayName(String displayName){
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder floorLevel(int floorLevel) {
            this.floorLevel = floorLevel;
            return this;
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public Builder subCategory(ItemSubCategory subCategory) {
            this.subCategory = subCategory;
            return this;
        }

        public Builder type(ItemType type) {
            this.type = type;
            return this;
        }

        public Builder rarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }


        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public FloorItem build(){
            return new FloorItem(this);
        }



    }
}
