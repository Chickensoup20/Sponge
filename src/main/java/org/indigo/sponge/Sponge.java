package org.indigo.sponge;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.indigo.sponge.rooms.Room;

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

        //Initialising commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(CommandHelper.flyspeedCommand().build(),List.of("fs","flightspeed"));
            commands.registrar().register(CommandHelper.devCommand());
            commands.registrar().register(CommandHelper.lobbyCommand());
            commands.registrar().register(CommandHelper.giveCommand(),List.of("give","get"));
            commands.registrar().register(CommandHelper.rooms());
        });

        // Plugin startup logic
        System.out.println("[Sponge] Plugin Enabled!");
        getServer().getPluginManager().registerEvents(new CancelledEvents(), this);
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
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

        //Loading rooms from files
        Path roomsDir = Path.of("rooms");

        if (Files.exists(roomsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(roomsDir, "*.json")) {
                for (Path file : stream) {
                    Room.fromFile(file.toString());
                }
            } catch (IOException | CorruptedWorldException | NewerFormatException | UnknownWorldException e) {
                throw new RuntimeException(e);
            }
        }



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Room room : rooms.values()){
            try {
                room.saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WorldGenerator();
    }

    public static boolean gameLoaded = false;
    public static List<Player> joinedPlayers = new ArrayList<>();
    public static HashMap<Player, SpongePlayer> playerStates = new HashMap<>();


    //Item Dictionaries (item id, value)
    public static HashMap<String, ItemStack> itemDic = new HashMap<>(); // Contains all items in the game
    public static HashMap<String, ItemStack> floorAllItems = new HashMap<>(); // Contains only items for all floors

    public static HashMap<String, HashMap<String, Double>> itemStatDic = new HashMap<>(); // Contains additional item stats

    //Room stuff
    public static HashMap<String,Room> rooms = new HashMap<>();
}
