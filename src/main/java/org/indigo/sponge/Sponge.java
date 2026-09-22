package org.indigo.sponge;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.indigo.sponge.registries.CustomBlocks;
import org.indigo.sponge.game.*;
import org.indigo.sponge.game.rooms.BuildEvents;
import org.indigo.sponge.registries.FloorRegistry;
import org.indigo.sponge.registries.Players;
import org.indigo.sponge.registries.RoomRegistry;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Sponge extends JavaPlugin {

    public static Plugin plugin;
    public static NamespacedKey spongeKey;
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static SlimeLoader loader;
    public static final AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();

    @Override
    public void onEnable() {
        //SlimeWorld loading
        loader = new FileLoader(new File("slime_worlds"));
        Players.init(new Players());

        CustomBlocks.init();
        File configFile = new File("config/paper-global.yml");
        if (!configFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("block-updates.disable-noteblock-updates", true);

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialising commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(CommandHelper.flyspeedCommand().build(),List.of("fs","flightspeed"));
            commands.registrar().register(CommandHelper.devCommand());
            commands.registrar().register(CommandHelper.lobbyCommand());
            commands.registrar().register(CommandHelper.giveCommand(),List.of("give","get"));
            commands.registrar().register(CommandHelper.rooms());
            commands.registrar().register(CommandHelper.testCommand());
            commands.registrar().register(CommandHelper.blocksCommand());
        });

        // Plugin startup logic
        System.out.println("[Sponge] Plugin Enabled!");
        getServer().getPluginManager().registerEvents(new CancelledEvents(), this);
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
        getServer().getPluginManager().registerEvents(new BuildEvents(), this);
        getServer().getPluginManager().registerEvents(new RunningGameEvents(), this);

        if(getServer().getWorld("lobby") == null) {
            new WorldCreator("lobby")
                .generator(new WorldGenerator())
                .createWorld();
        }
        plugin = getPlugin(Sponge.class);
        spongeKey = new NamespacedKey(plugin,"sponge");

        InitAll.makeWeapons();
        InitAll.makeArmors();
        InitAll.makeAccessories();
        InitAll.makeConsumables();

        for(World world : Bukkit.getWorlds()){
            world.setGameRule(GameRules.ADVANCE_TIME,false);
        }
        FloorRegistry.init(new FloorRegistry());
        FloorRegistry.register(new Floor("sponge", 1, List.of(RoomTemplate.RoomType.NORMAL, new Branch(List.of(RoomTemplate.RoomType.BOSS)), RoomTemplate.RoomType.BOSS)));
        RoomRegistry.init(new RoomRegistry());
        RoomRegistry.loadRooms();
    }

    @Override
    public void onDisable() {
        RoomRegistry.saveRooms();
        RoomRegistry.shutdown();
        FloorRegistry.shutdown();
        Players.shutdown();
    }

    private static final Gson GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override public boolean shouldSkipClass(Class<?> c) {
                    String n = c.getName();
                    return n.startsWith("org.bukkit.craftbukkit")
                            || n.startsWith("net.minecraft")
                            || org.bukkit.World.class.isAssignableFrom(c)
                            || org.bukkit.entity.Entity.class.isAssignableFrom(c)
                            || org.bukkit.Location.class.isAssignableFrom(c)
                            || com.infernalsuite.asp.api.world.SlimeWorld.class.isAssignableFrom(c);
                }
                @Override public boolean shouldSkipField(FieldAttributes f) { return false; }
            })
            .setPrettyPrinting()
            .create();

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WorldGenerator();
    }

}
