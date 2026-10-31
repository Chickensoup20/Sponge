package org.indigo.sponge.registries;

import org.indigo.sponge.item.FloorItem;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static ItemRegistry instance;
    private final Map<String, FloorItem> items = new HashMap<>();

    public static void init(ItemRegistry itemRegistry){
        instance = itemRegistry;
    }
    public static FloorItem get(String id){
        return instance.items.computeIfAbsent(id, n -> {
            throw new IllegalStateException("Unknown item: " + n);
        });
    }
    public static void register(FloorItem item) {
        instance.items.put(item.getId(),item);
    }
}
