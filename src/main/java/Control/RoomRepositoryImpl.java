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
    // AUTO-GENERATE ID (Standardized)
    // ==========================================
    @Override
    public String generateNextRoomId() {
        int maxId = 0;
        for (Room r : roomList) {
            String currentIdStr = r.getRoomNumber();
            if (currentIdStr != null && currentIdStr.toUpperCase().startsWith("R")) {
                try {
                    int currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore any badly formatted IDs
                }
            }
        }
        return String.format("R%03d", maxId + 1); 
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

    public ListInterface<Room> findAllOccupiedRooms() {
        return roomList.findAll(new SearchCriteria<Room>() {
            @Override
            public boolean isMatch(Room r) {
                return !r.isAvailable();
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
                try {
                    int num1 = Integer.parseInt(r1.getRoomNumber().replaceAll("\\D+", ""));
                    int num2 = Integer.parseInt(r2.getRoomNumber().replaceAll("\\D+", ""));
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    return r1.getRoomNumber().compareToIgnoreCase(r2.getRoomNumber());
                }
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
    // REPORT 1: UTILIZATION & OCCUPANCY (TEXT)
    // ==========================================
    @Override
    public String generateRoomReport(ListInterface<Appointment> allApts) {
        if (roomList.isEmpty()) {
            return "No room data available to generate report.\n";
        }

        ListInterface<Room> availableRooms = this.findAllAvailableRooms();
        ListInterface<Room> occupiedRooms = this.findAllOccupiedRooms();

        int totalRooms = roomList.getNumberOfEntries();
        int availableCount = availableRooms.getNumberOfEntries();
        int occupiedCount = occupiedRooms.getNumberOfEntries();

        ListInterface<String> roomTypes = new List<>();
        ListInterface<Integer> typeTotalCounts = new List<>();
        ListInterface<Integer> typeAvailableCounts = new List<>();

        for (Room r : roomList) {
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
        if (occupiedRooms.isEmpty()) { 
            report.append("All rooms are currently empty.\n");
        } else {
            for (Room r : occupiedRooms) {
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
        report.append("======================================================\n");
        report.append("End of Report.\n");

        return report.toString();
    }

    // ==========================================
    // REPORT 1: UTILIZATION & OCCUPANCY (HTML)
    // ==========================================
    @Override
    public String generateRoomHtmlReport(ListInterface<Appointment> allApts) {
        if (roomList.isEmpty()) {
            return "<h1>No Room Data Available to generate report.</h1>";
        }

        ListInterface<Room> availableRooms = this.findAllAvailableRooms();
        ListInterface<Room> occupiedRooms = this.findAllOccupiedRooms();

        int totalRooms = roomList.getNumberOfEntries();
        int availableCount = availableRooms.getNumberOfEntries();
        int occupiedCount = occupiedRooms.getNumberOfEntries();

        ListInterface<String> roomTypes = new List<>();
        ListInterface<Integer> typeTotalCounts = new List<>();
        ListInterface<Integer> typeAvailableCounts = new List<>();

        for (Room r : roomList) {
            String type = r.getRoomType();
            boolean found = false;
            
            for (int j = 1; j <= roomTypes.getNumberOfEntries(); j++) {
                if (roomTypes.getEntry(j).equalsIgnoreCase(type)) {
                    typeTotalCounts.replace(j, typeTotalCounts.getEntry(j) + 1); 
                    if (r.isAvailable()) {
                        typeAvailableCounts.replace(j, typeAvailableCounts.getEntry(j) + 1); 
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

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Clinic Room Report</title>\n");
        html.append("<style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; background: #f8f9fa; } table { width: 100%; border-collapse: collapse; background: white; } th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; } th { background-color: #34495e; color: white; } .occupied { background-color: #ffeaea; color: #c0392b; font-weight: bold; } .available { color: #27ae60; font-weight: bold; }</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<h1>🏥 Clinic Room Utilization Report</h1>\n");
        html.append("<p>Generated At: <strong>").append(time).append("</strong></p>\n");
        html.append("<p>Total Rooms: <strong>").append(totalRooms).append("</strong> | ");
        html.append("Available: <span class=\"available\">").append(availableCount).append("</span> | ");
        html.append("Occupied: <span class=\"occupied\">").append(occupiedCount).append("</span></p>\n");
        html.append("<p>Current Occupancy Rate: <strong>").append(String.format("%.2f%%", occupancyRate)).append("</strong></p>\n");

        html.append("<h2>Distribution by Room Type</h2>\n");
        html.append("<table>\n<tr><th>Room Type</th><th>Total Count</th><th>Available</th><th>Occupied</th></tr>\n");
        for (int k = 1; k <= roomTypes.getNumberOfEntries(); k++) {
            String rType = roomTypes.getEntry(k);
            int tCount = typeTotalCounts.getEntry(k);
            int aCount = typeAvailableCounts.getEntry(k);
            int oCount = tCount - aCount;
            html.append("<tr><td>").append(rType).append("</td><td>").append(tCount)
                .append("</td><td class=\"available\">").append(aCount)
                .append("</td><td class=\"occupied\">").append(oCount).append("</td></tr>\n");
        }
        html.append("</table>\n");

        html.append("<h2>Occupied Rooms Details</h2>\n");
        if (occupiedRooms.isEmpty()) { 
            html.append("<p>All rooms are currently empty.</p>\n");
        } else {
            html.append("<table>\n<tr><th>Room Number</th><th>Room Type</th><th>Occupant Name</th><th>Status</th></tr>\n");
            for (Room r : occupiedRooms) {
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
                html.append("<tr class=\"occupied\"><td>").append(r.getRoomNumber())
                    .append("</td><td>").append(r.getRoomType())
                    .append("</td><td>").append(occupantName)
                    .append("</td><td>").append(status).append("</td></tr>\n");
            }
            html.append("</table>\n");
        }

        html.append("</body>\n</html>");
        return html.toString();
    }

    // ==========================================
    // REPORT 2: AVAILABILITY DIRECTORY (TEXT)
    // ==========================================
    @Override
    public String generateAvailabilityDirectoryTextReport() {
        ListInterface<Room> availableRooms = this.findAllAvailableRooms();
        if (availableRooms.isEmpty()) {
            return "No rooms are currently available.\n";
        }

        ListInterface<String> uniqueTypes = new ADT.List<>();
        for (Room r : availableRooms) {
            boolean exists = false;
            for (int i = 1; i <= uniqueTypes.getNumberOfEntries(); i++) {
                if (uniqueTypes.getEntry(i).equalsIgnoreCase(r.getRoomType())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueTypes.add(r.getRoomType());
            }
        }

        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder report = new StringBuilder();
        report.append("\n======================================================\n");
        report.append("           CLINIC ROOM AVAILABILITY DIRECTORY         \n");
        report.append("======================================================\n");
        report.append("Generated At: ").append(time).append("\n\n");

        for (int i = 1; i <= uniqueTypes.getNumberOfEntries(); i++) {
            String currentType = uniqueTypes.getEntry(i);
            report.append("[ ").append(currentType.toUpperCase()).append(" ]\n");
            
            for (Room r : availableRooms) {
                if (r.getRoomType().equalsIgnoreCase(currentType)) {
                    report.append("  - Room ").append(r.getRoomNumber()).append("\n");
                }
            }
            report.append("\n");
        }
        report.append("======================================================\n");
        report.append("End of Directory.\n");
        
        return report.toString();
    }

    // ==========================================
    // REPORT 2: AVAILABILITY DIRECTORY (HTML)
    // ==========================================
    @Override
    public String generateAvailabilityDirectoryHtmlReport() {
        ListInterface<Room> availableRooms = this.findAllAvailableRooms();
        if (availableRooms.isEmpty()) {
            return "<h1>No Rooms are currently available.</h1>";
        }

        ListInterface<String> uniqueTypes = new ADT.List<>();
        for (Room r : availableRooms) {
            boolean exists = false;
            for (int i = 1; i <= uniqueTypes.getNumberOfEntries(); i++) {
                if (uniqueTypes.getEntry(i).equalsIgnoreCase(r.getRoomType())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueTypes.add(r.getRoomType());
            }
        }

        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Room Availability Directory</title>\n");
        html.append("<style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; background: #f8f9fa; } .category-card { background: white; padding: 15px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-left: 5px solid #27ae60; } h2 { color: #2c3e50; margin-top: 0; } .room-tag { display: inline-block; background: #e8f8f5; color: #27ae60; padding: 8px 15px; margin: 5px; border-radius: 20px; font-weight: bold; }</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<h1>✅ Clinic Room Availability Directory</h1>\n");
        html.append("<p>Generated At: <strong>").append(time).append("</strong></p>\n");

        for (int i = 1; i <= uniqueTypes.getNumberOfEntries(); i++) {
            String currentType = uniqueTypes.getEntry(i);
            html.append("<div class=\"category-card\">\n");
            html.append("<h2>").append(currentType.toUpperCase()).append("</h2>\n");
            html.append("<div>\n");
            
            for (Room r : availableRooms) {
                if (r.getRoomType().equalsIgnoreCase(currentType)) {
                    html.append("<span class=\"room-tag\">Room ").append(r.getRoomNumber()).append("</span>\n");
                }
            }
            html.append("</div>\n</div>\n");
        }

        html.append("</body>\n</html>");
        return html.toString();
    }
}