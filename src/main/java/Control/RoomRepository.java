package Control;

import ADT.ListInterface;
import Entity.Room;
import Entity.Appointment;

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
    
    // Sorting methods
    ListInterface<Room> sortedByRoomNumber();
    ListInterface<Room> sortedByType();
    
    // Reporting method
    String generateRoomReport(ListInterface<Appointment> allApts);
}