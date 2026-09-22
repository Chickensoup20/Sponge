package org.indigo.sponge.registries;

import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import org.indigo.sponge.game.RoomTemplate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RoomRegistry {
    private static RoomRegistry instance;

    private final Map<String, RoomTemplate> rooms = new HashMap<>();

    public static void init(RoomRegistry registry) {
        instance = registry;
    }

    public static void shutdown() {
        instance = null;
    }

    public static void register(RoomTemplate room) {
        instance().rooms.put(room.name, room);
    }

    public static RoomTemplate get(String name) {
        RoomTemplate room = instance().rooms.get(name);
        if (room == null) {
            throw new IllegalArgumentException("Unknown room: " + name);
        }
        return room;
    }

    public static void saveRooms() {
        for (RoomTemplate room : instance().rooms.values()) {
            try {
                room.saveToFile();
                room.unload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadRooms() {
        Path roomsDir = Path.of("room_templates");

        if (Files.exists(roomsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(roomsDir, "*.json")) {
                for (Path file : stream) {
                    String fileName = file.getFileName().toString();
                    String roomName = fileName.substring(0, fileName.length() - ".json".length());
                    register(RoomTemplate.fromFile(roomName));
                }
            } catch (IOException | CorruptedWorldException | NewerFormatException | UnknownWorldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Collection<RoomTemplate> all() {
        return Collections.unmodifiableCollection(instance().rooms.values());
    }

    public static Collection<RoomTemplate> all(RoomTemplate.RoomType roomType) {
        List<RoomTemplate> list = new ArrayList<>();
        for (RoomTemplate roomTemplate : instance().rooms.values()) {
            if (roomTemplate.roomType == roomType) {
                list.add(roomTemplate);
            }
        }
        return Collections.unmodifiableCollection(list);
    }

    private static RoomRegistry instance() {
        if (instance == null) {
            throw new IllegalStateException("RoomRegistry used before onEnable");
        }
        return instance;
    }
}
