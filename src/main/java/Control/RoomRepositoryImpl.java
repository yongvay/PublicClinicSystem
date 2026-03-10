/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.RoomDAO; 
import Entity.Room;

/**
 * @author Ng Zhi Xuan
 * Implementation of the RoomRepository using a custom List ADT.
 */
public class RoomRepositoryImpl implements RoomRepository {

    private ListInterface<Room> roomList;
    private RoomDAO roomDAO; // Declare the DAO for file handling

    public RoomRepositoryImpl() {
        this.roomDAO = new RoomDAO(); 
        
        // Load the data from the text file 
        this.roomList = roomDAO.loadFromFile(); 
        
        // Failsafe: If the file was completely empty or not found, initialize an empty list
        if (this.roomList == null) {
            this.roomList = new List<>();
        }
    }

    // ==========================================
    // CREATE
    // ==========================================
    @Override
    public void create(Room room) {
        if (room != null) {
            // Failsafe: Check if the room number already exists before adding
            if (findById(room.getRoomNumber()) == null) {
                roomList.add(room);
                roomDAO.saveToFile(roomList); // Save to file after adding
            } else {
                System.err.println("The entered Room Number already exists: " + room.getRoomNumber());
            }
        }
    }

    // ==========================================
    // READ
    // ==========================================
    @Override
    public ListInterface<Room> findAll() {
        return roomList;
    }

    @Override
    public Room findById(String roomNumber) {
        if (roomNumber == null) return null;
        
        for (Room r : roomList) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public ListInterface<Room> findByType(String type) {
        ListInterface<Room> results = new List<>();
        if (type == null || type.trim().isEmpty()) return results;

        String searchLower = type.toLowerCase();
        for (Room r : roomList) {
            if (r.getRoomType().toLowerCase().contains(searchLower)) {
                results.add(r);
            }
        }
        return results;
    }

    @Override
    public ListInterface<Room> findAllAvailableRooms() {
        ListInterface<Room> results = new List<>();
        for (Room r : roomList) {
            if (r.isAvailable()) {
                results.add(r);
            }
        }
        return results;
    }

    // ==========================================
    // UPDATE
    // ==========================================
    @Override
    public boolean update(Room updatedRoom) {
        if (updatedRoom == null) return false;

        // Iterate using 1-based indexing for your custom ADT
        for (int i = 1; i <= roomList.getNumberOfEntries(); i++) {
            Room current = roomList.getEntry(i);
            
            if (current.getRoomNumber().equalsIgnoreCase(updatedRoom.getRoomNumber())) {
                boolean success = roomList.replace(i, updatedRoom);
                if (success) {
                    roomDAO.saveToFile(roomList); // Save to file after updating
                }
                return success;
            }
        }
        return false;
    }

    // ==========================================
    // DELETE
    // ==========================================
    @Override
    public boolean delete(Room room) {
        if (room == null) return false;
        
        boolean success = roomList.remove(room);
        if (success) {
            roomDAO.saveToFile(roomList); // Save to file after deleting
        }
        return success;
    }
}