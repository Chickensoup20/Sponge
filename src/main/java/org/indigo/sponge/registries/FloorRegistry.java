package org.indigo.sponge.registries;

import org.indigo.sponge.game.Branch;
import org.indigo.sponge.game.Floor;
import org.indigo.sponge.game.RoomTemplate;

import java.util.*;

public class FloorRegistry {
    private static FloorRegistry instance;

    private final Map<String, Floor> floors = new HashMap<>();

    public static void init(FloorRegistry registry) {
        instance = registry;
    }

    public static void shutdown() {
        instance = null;
    }

    public static Floor get(String name) {
        return instance().floors.computeIfAbsent(name, n -> {
            throw new IllegalStateException("Unknown floor: " + n);
        });
    }

    public static void register(Floor floor) {
        instance().floors.put(floor.getFloorName(), floor);
    }

    private static FloorRegistry instance() {
        if (instance == null) {
            throw new IllegalStateException("FloorRegistry used before onEnable");
        }
        return instance;
    }
}
