package Boundary;

import Control.RoomRepository;
import Control.AppointmentRepository;
import Entity.Room;
import ADT.ListInterface;
import Utility.Utilities;
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
        System.out.println("8. View Sorted Rooms (By Number/Type)");
        System.out.println("9. View Room Report");
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
            case 8: viewSortedRooms(); break;
            case 9: generateRoomReport(); break;
            case 0: System.out.println("Exiting Room Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    private void addRoom() {
        System.out.println("\n--- Add New Room ---");
        String roomNumber = roomRepo.generateNextRoomId();
        System.out.println("Auto-generated Room Number: " + roomNumber);

        // UPDATED: Validation loop to prevent blank room types
        String roomType = "";
        while (roomType.trim().isEmpty()) {
            roomType = Utilities.getString("Enter Room Type (e.g., Consult, Treatment, Observation): ");
            if (roomType.trim().isEmpty()) {
                System.out.println("Error: Room Type cannot be empty. Please try again.");
            }
        }
        
        Room newRoom = new Room(roomNumber, roomType, true);
        
        if (roomRepo.create(newRoom)) {
            System.out.println("Success: Room added successfully!");
        } else {
            System.out.println("Error: Failed to add room.");
        }
    }

    private void viewAllRooms() {
        System.out.println("\n--- All Clinic Rooms ---");
        displayList(roomRepo.findAll());
    }

    private void searchByRoomNumber() {
        String roomNumber = Utilities.getString("\nEnter Room Number to search: ");
        Room found = roomRepo.findById(roomNumber);
        
        if (found != null) {
            System.out.println("Room Found: \n" + found.toString());
        } else {
            System.out.println("Room not found with Number: " + roomNumber);
        }
    }

    private void searchByRoomType() {
        String type = Utilities.getString("\nEnter Room Type to search (e.g., Treatment): ");
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
        Utilities.printHeader("Update Room Details");
        String roomNumber = Utilities.getString("Enter Room Number to update: ");
        Room existing = roomRepo.findById(roomNumber);
        
        if (existing == null) {
            System.out.println("Error: Room not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        String type = Utilities.getString("New Room Type [" + existing.getRoomType() + "]: ");
        updateIfNotEmpty(type, existing::setRoomType);

        String statusInput = Utilities.getString("Is Room Available? (Y/N) [" + (existing.isAvailable() ? "Y" : "N") + "]: ").trim();
        if (statusInput.equalsIgnoreCase("Y")) existing.setAvailable(true);
        else if (statusInput.equalsIgnoreCase("N")) existing.setAvailable(false);

        if (roomRepo.update(existing)) {
            System.out.println("Success: Room details updated successfully!");
        } else {
            System.out.println("Failed to update room details.");
        }
    }

    private void deleteRoom() {
        String roomNumber = Utilities.getString("\nEnter Room Number to delete: ");
        Room target = roomRepo.findById(roomNumber);
        
        if (target != null) {
            // UPDATED: Prevent deletion of occupied rooms to protect data integrity
            if (!target.isAvailable()) {
                System.out.println("Error: Cannot delete Room " + target.getRoomNumber() + " because it is currently occupied by a patient.");
                return;
            }

            String confirm = Utilities.getString("Are you sure you want to delete Room " + target.getRoomNumber() + "? (Y/N): ");
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

    private void viewSortedRooms() {
        System.out.println("\n--- Sort Rooms ---");
        System.out.println("1. Sort by Room Number");
        System.out.println("2. Sort by Room Type (A-Z)");
        System.out.print("Enter choice: ");

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();

            ListInterface<Room> sortedList = null;
            if (choice == 1) {
                sortedList = roomRepo.sortedByRoomNumber();
            } else if (choice == 2) {
                sortedList = roomRepo.sortedByType();
            } else {
                System.out.println("Invalid choice.");
                return;
            }

            displayList(sortedList);
        } else {
            System.out.println("Invalid input.");
            scanner.nextLine();
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
        String reportText = roomRepo.generateRoomReport(appointmentRepo.getAllAppointments());
        System.out.println(reportText);

        if (!reportText.equals("No room data available to generate report.\n")) {
            System.out.print("\nWould you like to export this report to a .txt file? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                Utilities.exportReportToFile(reportText, "RoomReport.txt");
            }
        }
    }

    // Helper method to enable functional interface updates
    private void updateIfNotEmpty(String input, java.util.function.Consumer<String> setter) {
        if (input != null && !input.trim().isEmpty()) {
            setter.accept(input.trim());
        }
    }
}