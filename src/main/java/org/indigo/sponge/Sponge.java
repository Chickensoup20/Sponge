package org.indigo.sponge;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sponge extends JavaPlugin {
    public static Plugin plugin;
    public static NamespacedKey spongeKey;

    @Override
    public void onEnable() {
        //Initialising commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(CommandHelper.flyspeedCommand().build());
        });

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
        plugin = getPlugin(Sponge.class);
        spongeKey = new NamespacedKey(plugin,"sponge");



        InitAll.MakeWeapons();
        InitAll.MakeArmors();
        InitAll.MakeAccessories();
        InitAll.MakeConsumables();

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

    public static boolean gameLoaded = false;
    public static List<Player> joinedPlayers = new ArrayList<>();
    public static HashMap<Player, SpongePlayer> playerStates = new HashMap<>();
    public static HashMap<String, ItemStack> itemDic = new HashMap<>();
}
