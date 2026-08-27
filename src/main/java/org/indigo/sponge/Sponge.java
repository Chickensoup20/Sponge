package org.indigo.sponge;

import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sponge extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("[Sponge] Plugin Enabled!");
        getServer().getPluginManager().registerEvents(new CancelledEvents(), this);
        getServer().getPluginManager().registerEvents(new ManagementEvents(), this);
        if(getServer().getWorld("lobby") == null) {
            new WorldCreator("lobby")
                .generator(new WorldGenerator())
                .createWorld();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WorldGenerator();
    }



}
