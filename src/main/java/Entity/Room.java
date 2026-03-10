/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

/**
 * @author Ng Zhi Xuan
 */
public class Room {
    
    private String roomNumber;
    private String roomType;
    private boolean isAvailable;

    public Room(String roomNumber, String roomType, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isAvailable = isAvailable;
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public String getRoomNumber() { 
        return roomNumber; 
    }
    
    public String getRoomType() { 
        return roomType; 
    }
    
    public boolean isAvailable() { 
        return isAvailable; 
    }

    // ==========================================
    // SETTERS
    // ==========================================
    public void setRoomNumber(String roomNumber) { 
        this.roomNumber = roomNumber; 
    }
    
    public void setRoomType(String roomType) { 
        this.roomType = roomType; 
    }
    
    public void setAvailable(boolean isAvailable) { 
        this.isAvailable = isAvailable; 
    }

    // ==========================================
    // OVERRIDDEN METHODS
    // ==========================================
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Room room = (Room) obj;
        // Two rooms are considered equal if they have the same Room Number
        return roomNumber != null && roomNumber.equalsIgnoreCase(room.roomNumber);
    }

    @Override
    public String toString() {
        String statusStr = isAvailable ? "Available" : "Occupied";
        return String.format("Room No: %-5s | Type: %-15s | Status: %s", 
                roomNumber, roomType, statusStr);
    }
}
