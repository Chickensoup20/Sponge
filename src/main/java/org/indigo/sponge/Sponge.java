package org.indigo.sponge;

import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Sponge extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("[Sponge] Plugin Enabled!");
        getServer().getPluginManager().registerEvents(new CancelledEvents(), this);
        getServer().getPluginManager().registerEvents(new ManagementEvents(), this);
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
        if(getServer().getWorld("lobby") == null) {
            new WorldCreator("lobby")
                .generator(new WorldGenerator())
                .createWorld();
        }

    }
//test
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WorldGenerator();
    }

    public static List<Player> joinedPlayers;
}
