package org.indigo.sponge.registries;

import org.bukkit.entity.Player;
import org.indigo.sponge.SpongePlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Players {
    private static Players instance;

    private final Map<UUID, SpongePlayer> players = new HashMap<>();

    public static void init(Players registry) {
        instance = registry;
    }

    public static void shutdown() {
        instance = null;
    }

    public static void register(SpongePlayer session) {
        instance().players.put(session.getUniqueId(), session);
    }

    public static SpongePlayer find(Player player) {
        return find(player.getUniqueId());
    }

    public static SpongePlayer find(UUID uuid) {
        return instance().players.get(uuid);
    }

    public static SpongePlayer get(Player player) {
        return get(player.getUniqueId());
    }

    public static SpongePlayer get(UUID uuid) {
        SpongePlayer session = find(uuid);
        if (session == null) {
            throw new IllegalStateException("No session for player " + uuid);
        }
        return session;
    }

    public static void remove(UUID uuid) {
        instance().players.remove(uuid);
    }

    public static Collection<SpongePlayer> all() {
        return Collections.unmodifiableCollection(instance().players.values());
    }

    private static Players instance() {
        if (instance == null) {
            throw new IllegalStateException("Players used before onEnable");
        }
        return instance;
    }
}
