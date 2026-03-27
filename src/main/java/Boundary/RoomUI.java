package Boundary;

import Control.RoomRepository;
import Control.AppointmentRepository;
import Entity.Room;
import Entity.Appointment;
import ADT.ListInterface;
import ADT.List; 
import java.util.Scanner;

/**
 * @author Ng Zhi Xuan
 */
public class RoomUI {

    private RoomRepository roomRepo;
    private AppointmentRepository appointmentRepo;
    private Scanner scanner;

    public RoomUI(RoomRepository roomRepo, AppointmentRepository appointmentRepo) {
        this.roomRepo = roomRepo;
        this.appointmentRepo = appointmentRepo;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n==========================================");
        System.out.println("        CLINIC SUBSYSTEM: ROOM MENU       ");
        System.out.println("==========================================");
        System.out.println("1. Add New Room");
        System.out.println("2. View All Rooms");
        System.out.println("3. Search Room by Room Number");
        System.out.println("4. Search Room by Type");
        System.out.println("5. View Available Rooms");
        System.out.println("6. Update Room Details");
        System.out.println("7. Delete Room");
        System.out.println("8. View Room Report");
        System.out.println("0. Exit to Main Menu");
        System.out.println("==========================================");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: addRoom(); break;
            case 2: viewAllRooms(); break;
            case 3: searchByRoomNumber(); break;
            case 4: searchByRoomType(); break;
            case 5: viewAvailableRooms(); break;
            case 6: updateRoom(); break;
            case 7: deleteRoom(); break;
            case 8: generateRoomReport(); break;
            case 0: System.out.println("Exiting Room Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    private void addRoom() {
        System.out.println("\n--- Add New Room ---");
        System.out.print("Enter Room Number (e.g., 101): ");
        String roomNumber = scanner.nextLine();
        
        if (roomRepo.findById(roomNumber) != null) {
            System.out.println("Error: Room Number already exists!");
            return;
        }

        System.out.print("Enter Room Type (e.g., Consult, Treatment, Observation): ");
        String roomType = scanner.nextLine();
        
        Room newRoom = new Room(roomNumber, roomType, true);
        roomRepo.create(newRoom);
        System.out.println("Success: Room added successfully!");
    }

    private void viewAllRooms() {
        System.out.println("\n--- All Clinic Rooms ---");
        displayList(roomRepo.findAll());
    }

    private void searchByRoomNumber() {
        System.out.print("\nEnter Room Number to search: ");
        String roomNumber = scanner.nextLine();
        Room found = roomRepo.findById(roomNumber);
        
        if (found != null) {
            System.out.println("Room Found: \n" + found.toString());
        } else {
            System.out.println("Room not found with Number: " + roomNumber);
        }
    }

    private void searchByRoomType() {
        System.out.print("\nEnter Room Type to search (e.g., Treatment): ");
        String type = scanner.nextLine();
        ListInterface<Room> results = roomRepo.findByType(type);
        
        if (results.isEmpty()) {
            System.out.println("No rooms found of type: " + type);
        } else {
            System.out.println("Search Results:");
            displayList(results);
        }
    }

    private void viewAvailableRooms() {
        System.out.println("\n--- Currently Available Rooms ---");
        ListInterface<Room> availableRooms = roomRepo.findAllAvailableRooms();
        if (availableRooms.isEmpty()) {
            System.out.println("All rooms are currently occupied.");
        } else {
            displayList(availableRooms);
        }
    }

    private void updateRoom() {
        System.out.print("\nEnter Room Number to update: ");
        String roomNumber = scanner.nextLine();
        Room existing = roomRepo.findById(roomNumber);
        
        if (existing == null) {
            System.out.println("Error: Room not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        System.out.print("New Room Type [" + existing.getRoomType() + "]: ");
        String type = scanner.nextLine();
        if (!type.isEmpty()) existing.setRoomType(type);

        System.out.print("Is Room Available? (Y/N) [" + (existing.isAvailable() ? "Y" : "N") + "]: ");
        String statusInput = scanner.nextLine().trim();
        if (statusInput.equalsIgnoreCase("Y")) existing.setAvailable(true);
        else if (statusInput.equalsIgnoreCase("N")) existing.setAvailable(false);

        if (roomRepo.update(existing)) {
            System.out.println("Success: Room details updated successfully!");
        } else {
            System.out.println("Failed to update room details.");
        }
    }

    private void deleteRoom() {
        System.out.print("\nEnter Room Number to delete: ");
        String roomNumber = scanner.nextLine();
        Room target = roomRepo.findById(roomNumber);
        
        if (target != null) {
            System.out.print("Are you sure you want to delete Room " + target.getRoomNumber() + "? (Y/N): ");
            String confirm = scanner.nextLine();
            if (confirm.equalsIgnoreCase("Y")) {
                if (roomRepo.delete(target)) {
                    System.out.println("Success: Room deleted.");
                } else {
                    System.out.println("Error: Could not delete room.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("Error: Room not found.");
        }
    }

    private void displayList(ListInterface<Room> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Room r : list) {
            System.out.println(r.toString());
        }
    }

    public void generateRoomReport() {
        ListInterface<Room> list = roomRepo.findAll();
        if (list.isEmpty()) {
            System.out.println("No room data available.");
            return;
        }

        int totalRooms = list.getNumberOfEntries();
        int availableCount = 0;
        int occupiedCount = 0;

        ListInterface<String> roomTypes = new List<>();
        ListInterface<Integer> typeTotalCounts = new List<>();
        ListInterface<Integer> typeAvailableCounts = new List<>();

        for (int i = 1; i <= totalRooms; i++) {
            Room r = list.getEntry(i);

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

        System.out.println("\n======================");
        System.out.println("==== ROOM REPORT ====");
        System.out.println("======================");
        System.out.println("Generated At: " + time);
        System.out.println("Total Rooms: " + totalRooms);

        System.out.println("\n--- Overall Utilization ---");
        System.out.println("Available Rooms: " + availableCount);
        System.out.println("Occupied Rooms: " + occupiedCount);
        System.out.println("Current Occupancy Rate: " + String.format("%.2f%%", occupancyRate));

        System.out.println("\n--- Distribution by Room Type ---");
        for (int k = 1; k <= roomTypes.getNumberOfEntries(); k++) {
            String rType = roomTypes.getEntry(k);
            int tCount = typeTotalCounts.getEntry(k);
            int aCount = typeAvailableCounts.getEntry(k);
            
            System.out.printf("%-15s : %2d Total ( %2d Available, %2d Occupied )\n", 
                    rType, tCount, aCount, (tCount - aCount));
        }

        System.out.println("\n--- Occupied Rooms Details ---");
        if (occupiedCount == 0) {
            System.out.println("All rooms are currently empty.");
        } else {
            ListInterface<Appointment> allApts = appointmentRepo.getAllAppointments();
            
            for (int i = 1; i <= totalRooms; i++) {
                Room r = list.getEntry(i);
                
                if (!r.isAvailable()) {
                    String occupantName = "Unknown Patient";
                    String status = "";
                    
                    for (int j = 1; j <= allApts.getNumberOfEntries(); j++) {
                        Appointment apt = allApts.getEntry(j);
                        
                        if (apt.getRoom() != null && apt.getRoom().getRoomNumber().equals(r.getRoomNumber())) {
                            if (apt.getStatus().equalsIgnoreCase("Scheduled") || apt.getStatus().equalsIgnoreCase("Admitted")) {
                                occupantName = apt.getPatient().getPatientName();
                                status = apt.getStatus();
                                break;
                            }
                        }
                    }
                    System.out.printf("Room %-4s (%-12s) - Occupied by: %s [%s]\n", r.getRoomNumber(), r.getRoomType(), occupantName, status);
                }
            }
        }
        System.out.println("======================\n");
    }
}