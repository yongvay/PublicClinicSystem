package Control;

import ADT.ListInterface;
import Entity.Room;
import Entity.Appointment;

/**
 * @author Ng Zhi Xuan
 */
public interface RoomRepository {
    
    String generateNextRoomId();
    boolean create(Room room);
    ListInterface<Room> findAll();
    Room findById(String roomNumber);
    ListInterface<Room> findByType(String type);
    ListInterface<Room> findAllAvailableRooms();
    boolean update(Room updatedRoom);
    boolean delete(Room room);
    ListInterface<Room> sortedByRoomNumber();
    ListInterface<Room> sortedByType();
    
    // REPORT 1: Utilization & Occupancy
    String generateRoomReport(ListInterface<Appointment> allApts);
    String generateRoomHtmlReport(ListInterface<Appointment> allApts);

    // REPORT 2: Availability Directory
    String generateAvailabilityDirectoryTextReport();
    String generateAvailabilityDirectoryHtmlReport();
}