package org.indigo.sponge.rooms;

import com.google.gson.Gson;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.indigo.sponge.Sponge.*;


public class Room {
    private transient SlimeWorld slimeWorld;
    private transient World world;
    private String name;
    public Room(String name, int floor){
        slimeWorld = asp.createEmptyWorld(name,false, new SlimePropertyMap(),loader);
        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld,false);
        world = worldInstance.getBukkitWorld();
        this.name = name;
        for(int x = -500; x < 500; x++){
            for(int z = -500; z < 500; z++){
                new Location(world,x,50,z).getBlock().setType(Material.BEDROCK);
            }
        }
        rooms.put(name,this);
    }
    public void tpToWorld(Player player){
        if(!asp.worldLoaded(slimeWorld)){
            loadWorld();
        }
        player.teleport(new Location(world, 0,51,0));
    }

    public void loadWorld(){
        SlimeWorldInstance worldInstance = asp.loadWorld(slimeWorld,false);
        world = worldInstance.getBukkitWorld();
    }

    public void saveToFile() throws IOException {
        Gson gson = new Gson();
        Path path = Path.of("rooms/" + name + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, gson.toJson(this));
    }

    public Room() {}

    public static Room fromFile(String fileName) throws IOException, CorruptedWorldException, NewerFormatException, UnknownWorldException {
        Room room = new Gson().fromJson(Files.readString(Path.of(fileName)), Room.class);
        // now reconstruct the live objects yourself
        room.slimeWorld = asp.readWorld(loader,room.name,false,new SlimePropertyMap());
        SlimeWorldInstance worldInstance = asp.loadWorld(room.slimeWorld,false);
        room.world = worldInstance.getBukkitWorld();
        rooms.put(room.name,room);
        return room;
    }

}
