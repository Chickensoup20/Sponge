package org.indigo.sponge;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.indigo.sponge.block.CustomBlock;
import org.indigo.sponge.block.CustomBlocks;
import org.indigo.sponge.functions.Utils;
import org.indigo.sponge.game.*;
import org.indigo.sponge.game.rooms.BuildEvents;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
            commands.registrar().register(CommandHelper.pathCommand());
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

        //Creating Floors
        Floor floor1 = new Floor("sponge",1,List.of(RoomTemplate.RoomType.NORMAL,new Branch(List.of(RoomTemplate.RoomType.BOSS))));

        //Loading rooms from files
        Path roomsDir = Path.of("room_templates");

        if (Files.exists(roomsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(roomsDir, "*.json")) {
                for (Path file : stream) {
                    String fileName = file.getFileName().toString();
                    String roomName = fileName.substring(0, fileName.length() - ".json".length()); // "test"
                    RoomTemplate.fromFile(roomName);
                }
            } catch (IOException | CorruptedWorldException | NewerFormatException | UnknownWorldException e) {
                throw new RuntimeException(e);
            }
        }

        Path pathsDir = Path.of("lobby/paths.json");
        try {
            pathPoints = GSON.fromJson(Files.readString(pathsDir), List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pathsDir = Path.of("lobby/docks.json");
        try {
            dockPoints = GSON.fromJson(Files.readString(pathsDir), List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pathsDir = Path.of("lobby/dockRots.json");
        try {
            dockRotations = GSON.fromJson(Files.readString(pathsDir), List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static ItemStack entranceWand = Utils.createItem(Material.BLAZE_ROD,Colors.toMM(Colors.ORANGE_LIGHT) + "Entrance Wand","entrancewand");
    public static ItemStack exitWand = Utils.createItem(Material.BREEZE_ROD,Colors.toMM(Colors.SKY_LIGHT) + "Exit Wand","exitwand");

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(RoomTemplate room : allRooms.values()){
            try {
                room.saveToFile();
                room.unload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path path = Path.of("lobby/paths.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(pathPoints));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        path = Path.of("lobby/docks.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(dockPoints));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        path = Path.of("lobby/dockRots.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(dockRotations));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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

    public static boolean gameLoaded = false;
    public static List<Player> joinedPlayers = new ArrayList<>();
    public static HashMap<Player, SpongePlayer> players = new HashMap<>();


    //Item Dictionaries (item id, value)
    public static HashMap<String, ItemStack> itemDic = new HashMap<>(); // Contains all items in the game
    public static HashMap<String, ItemStack> floorAllItems = new HashMap<>(); // Contains only items for all floors

    public static HashMap<String, HashMap<String, Double>> itemStatDic = new HashMap<>(); // Contains additional item stats

    //Room stuff
    public static HashMap<String,Floor> floors = new HashMap<>();
    public static HashMap<String, RoomTemplate> allRooms = new HashMap<>();
    public static List<Game> runningGames = new ArrayList<>();
    public static List<RoomTemplate> branchRooms = new ArrayList<>();

    //Spawn visuals
    public static List<Vector> pathPoints = new ArrayList<>();
    public static List<Vector> dockPoints = new ArrayList<>();
    public static List<Integer> dockRotations = new ArrayList<>();

    //Custom Block Stuff
    public static List<CustomBlock> customBlocks = new ArrayList<>();
}
