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



    public Floor(String floorName, int floorLevel, List<RoomNode> floorMap){
        this.floorLevel = floorLevel;
        this.name = floorName;
        this.floorMap = floorMap;
        for(RoomTemplate.RoomType roomType : RoomTemplate.RoomType.values()){
            rooms.put(roomType,new ArrayList<>());
        }
    }

    public void addRoom(RoomTemplate room){
        allRooms.add(room);
        rooms.get(room.roomType).add(room);
    }

    public void removeRoom(RoomTemplate room){
        allRooms.remove(room);
    }
}