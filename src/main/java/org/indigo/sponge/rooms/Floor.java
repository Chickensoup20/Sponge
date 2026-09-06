package org.indigo.sponge.rooms;

import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private String floorName;
    private int floorLevel;
    private List<RoomTemplate> rooms = new ArrayList<>();

    public Floor(String floorName, int floorLevel){
        this.floorLevel = floorLevel;
        this.floorName = floorName;
        Sponge.floors.put(floorName,this);
    }

    public void addRoom(RoomTemplate room){
        rooms.add(room);
    }

    public void removeRoom(RoomTemplate room){
        rooms.remove(room);
    }
}
