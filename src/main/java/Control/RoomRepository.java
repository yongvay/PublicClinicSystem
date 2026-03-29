/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import ADT.ListInterface;
import Entity.Room;

/**
 * @author Ng Zhi Xuan
 */
public interface RoomRepository {

    // Auto Generate Room Number
    String generateNextRoomId();

    // Create 
    boolean create(Room room);

    // Read 
    ListInterface<Room> findAll();
    Room findById(String roomNumber);
    ListInterface<Room> findByType(String type);
    ListInterface<Room> findAllAvailableRooms();

    // Update 
    boolean update(Room room);

    // Delete 
    boolean delete(Room room);
}