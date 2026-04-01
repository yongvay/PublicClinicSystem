package Control;

import ADT.List;
import ADT.ListInterface;
import ADT.SearchCriteria;
import DAO.RoomDAO; 
import Entity.Room;
import Entity.Appointment;
import java.util.Comparator;

/**
 * @author Ng Zhi Xuan
 * Implementation of the RoomRepository using a custom List ADT.
 */
public class RoomRepositoryImpl implements RoomRepository {

    private ListInterface<Room> roomList;
    private RoomDAO roomDAO; 

    public RoomRepositoryImpl() {
        this.roomDAO = new RoomDAO(); 
        this.roomList = roomDAO.loadFromFile(); 
        
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
        for (Room r : roomList) {
            String currentIdStr = r.getRoomNumber();
            if (currentIdStr != null) {
                try {
                    int currentIdNum;
                    if (currentIdStr.toUpperCase().startsWith("R")) {
                        currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    } else {
                        currentIdNum = Integer.parseInt(currentIdStr);
                    }
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore badly formatted numbers
                }
            }
        }
        if (maxId == 0) return "101";
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
                return true; 
            }
        }
        return false; 
    }

    // ==========================================
    // READ (Using SearchCriteria)
    // ==========================================
    @Override
    public ListInterface<Room> findAll() {
        return roomList;
    }

    @Override
    public Room findById(final String roomNumber) {
        if (roomNumber == null) return null;
        
        return roomList.findFirst(new SearchCriteria<Room>() {
            @Override
            public boolean isMatch(Room r) {
                return r.getRoomNumber().equalsIgnoreCase(roomNumber);
            }
        });
    }

    @Override
    public ListInterface<Room> findByType(String type) {
        if (type == null || type.trim().isEmpty()) return new List<>();

        final String searchLower = type.toLowerCase();
        
        return roomList.findAll(new SearchCriteria<Room>() {
            @Override
            public boolean isMatch(Room r) {
                return r.getRoomType().toLowerCase().contains(searchLower);
            }
        });
    }

    @Override
    public ListInterface<Room> findAllAvailableRooms() {
        return roomList.findAll(new SearchCriteria<Room>() {
            @Override
            public boolean isMatch(Room r) {
                return r.isAvailable();
            }
        });
    }

    // ==========================================
    // UPDATE (Using getPosition)
    // ==========================================
    @Override
    public boolean update(Room updatedRoom) {
        if (updatedRoom == null) return false;

        int position = roomList.getPosition(updatedRoom);

        if (position != -1) {
            boolean success = roomList.replace(position, updatedRoom);
            if (success) {
                roomDAO.saveToFile(roomList);
            }
            return success;
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
            roomDAO.saveToFile(roomList); 
        }
        return success;
    }

    // ==========================================
    // SORTING (Using Comparators)
    // ==========================================
    @Override
    public ListInterface<Room> sortedByRoomNumber() {
        return roomList.sort(new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                return r1.getRoomNumber().compareToIgnoreCase(r2.getRoomNumber());
            }
        });
    }

    @Override
    public ListInterface<Room> sortedByType() {
        return roomList.sort(new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                return r1.getRoomType().compareToIgnoreCase(r2.getRoomType());
            }
        });
    }

    // ==========================================
    // REPORT GENERATION
    // ==========================================
    @Override
    public String generateRoomReport(ListInterface<Appointment> allApts) {
        if (roomList.isEmpty()) {
            return "No room data available to generate report.\n";
        }

        int totalRooms = roomList.getNumberOfEntries();
        int availableCount = 0;
        int occupiedCount = 0;

        ListInterface<String> roomTypes = new List<>();
        ListInterface<Integer> typeTotalCounts = new List<>();
        ListInterface<Integer> typeAvailableCounts = new List<>();

        for (Room r : roomList) {
            if (r.isAvailable()) {
                availableCount++;
            } else {
                occupiedCount++;
            }

            String type = r.getRoomType();
            boolean found = false;
            
            for (int j = 1; j <= roomTypes.getNumberOfEntries(); j++) {
                if (roomTypes.getEntry(j).equalsIgnoreCase(type)) {
                    int currentTotal = typeTotalCounts.getEntry(j);
                    typeTotalCounts.replace(j, currentTotal + 1);
                    
                    if (r.isAvailable()) {
                        int currentAvail = typeAvailableCounts.getEntry(j);
                        typeAvailableCounts.replace(j, currentAvail + 1);
                    }
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                roomTypes.add(type);
                typeTotalCounts.add(1);
                typeAvailableCounts.add(r.isAvailable() ? 1 : 0);
            }
        }

        double occupancyRate = (double) occupiedCount / totalRooms * 100;
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder report = new StringBuilder();
        report.append("\n======================================================\n");
        report.append("                   CLINIC ROOM REPORT                 \n");
        report.append("======================================================\n");
        report.append("Generated At: ").append(time).append("\n");
        report.append("Total Rooms: ").append(totalRooms).append("\n");

        report.append("\n[1] OVERALL UTILIZATION\n");
        report.append("------------------------------------------------------\n");
        report.append("Available Rooms: ").append(availableCount).append("\n");
        report.append("Occupied Rooms: ").append(occupiedCount).append("\n");
        report.append("Current Occupancy Rate: ").append(String.format("%.2f%%", occupancyRate)).append("\n");

        report.append("\n[2] DISTRIBUTION BY ROOM TYPE\n");
        report.append("------------------------------------------------------\n");
        for (int k = 1; k <= roomTypes.getNumberOfEntries(); k++) {
            String rType = roomTypes.getEntry(k);
            int tCount = typeTotalCounts.getEntry(k);
            int aCount = typeAvailableCounts.getEntry(k);
            
            report.append(String.format("%-15s : %2d Total ( %2d Available, %2d Occupied )\n", 
                    rType, tCount, aCount, (tCount - aCount)));
        }

        report.append("\n[3] OCCUPIED ROOMS DETAILS\n");
        report.append("------------------------------------------------------\n");
        if (occupiedCount == 0) {
            report.append("All rooms are currently empty.\n");
        } else {
            for (Room r : roomList) {
                if (!r.isAvailable()) {
                    String occupantName = "Unknown Patient";
                    String status = "";
                    
                    for (Appointment apt : allApts) {
                        if (apt.getRoom() != null && apt.getRoom().getRoomNumber().equals(r.getRoomNumber())) {
                            if (apt.getStatus().equalsIgnoreCase("Scheduled") || apt.getStatus().equalsIgnoreCase("Admitted")) {
                                occupantName = apt.getPatient().getPatientName();
                                status = apt.getStatus();
                                break;
                            }
                        }
                    }
                    report.append(String.format("Room %-4s (%-12s) - Occupied by: %s [%s]\n", r.getRoomNumber(), r.getRoomType(), occupantName, status));
                }
            }
        }
        report.append("======================================================\n");
        report.append("End of Report.\n");

        return report.toString();
    }
}