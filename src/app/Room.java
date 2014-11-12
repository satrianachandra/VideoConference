/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author chandra
 */
public class Room {
    /** Room ID, from 1 to 254 included */
    protected int id;
    /** People who are currently in the room */
    private Set<String> audience = new TreeSet<String>();
    public Room(int id) {
        this.id = id;
    }
    /**
    * Used to print the room in the UI.
    *
    * @return the string with the room Id, example "Room 42"
    */
    public String toString() {
        return ("Room " + id);
    }
}