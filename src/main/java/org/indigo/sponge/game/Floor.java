package org.indigo.sponge.game;

import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Floor {
    public List<RoomTemplate> getAllRooms() {
        return allRooms;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public String getFloorName() {
        return name;
    }

    private String name;
    private int floorLevel;
    private List<RoomTemplate> allRooms = new ArrayList<>();
    public HashMap<RoomTemplate.RoomType, List<RoomTemplate>> rooms = new HashMap<>();
    public List<RoomNode> floorMap;



    /**
     * Creates a new floor and registers it in the global floor lookup.
     * @param floorName unique name used to identify and later retrieve this floor
     * @param floorLevel vertical/ordering level of this floor
     */
    public Floor(String floorName, int floorLevel, List<RoomNode> floorMap){
        this.floorLevel = floorLevel;
        this.name = floorName;
        this.floorMap = floorMap;
        for(RoomTemplate.RoomType roomType : RoomTemplate.RoomType.values()){
            rooms.put(roomType,new ArrayList<>());
        }
    }

    /**
     * Adds a room template to this floor's room list.
     * @param room the room template to add
     */
    public void addRoom(RoomTemplate room){
        allRooms.add(room);
        rooms.get(room.roomType).add(room);
    }

    /**
     * Removes a room template from this floor's room list.
     * @param room the room template to remove
     */
    public void removeRoom(RoomTemplate room){
        allRooms.remove(room);
    }
}