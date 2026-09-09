package org.indigo.sponge.rooms;

import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private String floorName;
    private int floorLevel;
    private List<RoomTemplate> rooms = new ArrayList<>();

    /**
     * Creates a new floor and registers it in the global floor lookup.
     * @param floorName unique name used to identify and later retrieve this floor
     * @param floorLevel vertical/ordering level of this floor
     */
    public Floor(String floorName, int floorLevel){
        this.floorLevel = floorLevel;
        this.floorName = floorName;
        Sponge.floors.put(floorName,this);
    }

    /**
     * Adds a room template to this floor's room list.
     * @param room the room template to add
     */
    public void addRoom(RoomTemplate room){
        rooms.add(room);
    }

    /**
     * Removes a room template from this floor's room list.
     * @param room the room template to remove
     */
    public void removeRoom(RoomTemplate room){
        rooms.remove(room);
    }
}