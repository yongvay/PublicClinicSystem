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
    // AUTO-GENERATE ID
    // ==========================================
    @Override
    public String generateNextRoomId() {
        int maxId = 0;
        
        // Loop through the custom Iterable List to find the highest room number
        for (Room r : roomList) {
            String currentIdStr = r.getRoomNumber();
            
            if (currentIdStr != null) {
                try {
                    int currentIdNum;
                    // Check if it has a prefix like "R101" or if it's purely numeric like "101"
                    if (currentIdStr.toUpperCase().startsWith("R")) {
                        currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    } else {
                        currentIdNum = Integer.parseInt(currentIdStr);
                    }
                    
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore any badly formatted Room Numbers
                }
            }
        }
        
        // If the list is empty, start at 101. Otherwise, increment the max found.
        if (maxId == 0) {
            return "101";
        }
        
        // Return the next number as a String. 
        return String.valueOf(maxId + 1); 
    }

    // ==========================================
    // CREATE
    // ==========================================
    @Override
    public boolean create(Room room) {
        if (room != null) {
            if (findById(room.getRoomNumber()) == null) {
                roomList.add(room);
                roomDAO.saveToFile(roomList); 
                return true; // Success
            }
        }
        return false; // Failed (Already exists or null)
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