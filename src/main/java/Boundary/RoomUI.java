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
    
    // Define the strictly allowed room types for the clinic
    private final String[] ALLOWED_ROOM_TYPES = {
        "Consultation", "Treatment", "Observation", "Ward", "Surgery", "Pharmacy"
    };

    public RoomUI(RoomRepository roomRepo, AppointmentRepository appointmentRepo) {
        this.roomRepo = roomRepo;
        this.appointmentRepo = appointmentRepo;
        this.scanner = new Scanner(System.in);
    }

    // ==========================================
    // MENU & NAVIGATION
    // ==========================================
    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim(); 
            
            try {
                choice = Integer.parseInt(input); 
                processChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a numerical value.");
                choice = -1; 
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
        System.out.println("9. Generate Utilization Report");
        System.out.println("10. Generate Availability Directory Report");
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
            case 9: generateUtilizationReport(); break;
            case 10: generateAvailabilityDirectory(); break;
            case 0: System.out.println("Exiting Room Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    // ==========================================
    // CORE FUNCTIONALITIES
    // ==========================================
    private void addRoom() {
        System.out.println("\n--- Add New Room ---");
        String roomNumber = roomRepo.generateNextRoomId();
        System.out.println("Auto-generated Room Number: " + roomNumber);

        String roomType = getValidRoomType(false, "");
        
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
        String roomNumber = getValidRoomNumberInput("\nEnter Room Number to search (e.g., R001): ");
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
            System.out.println("No rooms found containing type: " + type);
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
        String roomNumber = getValidRoomNumberInput("Enter Room Number to update (e.g., R001): ");
        Room existing = roomRepo.findById(roomNumber);
        
        if (existing == null) {
            System.out.println("Error: Room not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        // Validate Room Type Update
        String newType = getValidRoomType(true, existing.getRoomType());
        if (!newType.isEmpty()) {
            existing.setRoomType(newType);
        }

        // Validate Availability Status Update
        if (!existing.isAvailable()) {
            System.out.println("Status: This room is currently occupied. Status cannot be manually changed. Please process patient discharge in the Appointment System to free the room.");
        } else {
            String statusInput = "";
            while (true) {
                statusInput = Utilities.getString("Is Room Available? (Y/N) [" + (existing.isAvailable() ? "Y" : "N") + "]: ").trim();
                if (statusInput.equalsIgnoreCase("Y") || statusInput.equalsIgnoreCase("N") || statusInput.isEmpty()) {
                    break;
                }
                System.out.println("Error: Please enter 'Y' for Yes or 'N' for No, or press Enter to skip.");
            }
            
            if (statusInput.equalsIgnoreCase("Y")) existing.setAvailable(true);
            else if (statusInput.equalsIgnoreCase("N")) existing.setAvailable(false);
        }

        if (roomRepo.update(existing)) {
            System.out.println("Success: Room details updated successfully!");
        } else {
            System.out.println("Failed to update room details.");
        }
    }

    private void deleteRoom() {
        String roomNumber = getValidRoomNumberInput("\nEnter Room Number to delete (e.g., R001): ");
        Room target = roomRepo.findById(roomNumber);
        
        if (target != null) {
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
        while (true) {
            System.out.println("\n--- Sort Rooms ---");
            System.out.println("1. Sort by Room Number");
            System.out.println("2. Sort by Room Type (A-Z)");
            System.out.println("0. Cancel");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice == 0) return; 
                
                ListInterface<Room> sortedList = null;
                if (choice == 1) {
                    sortedList = roomRepo.sortedByRoomNumber();
                    displayList(sortedList);
                    break;
                } else if (choice == 2) {
                    sortedList = roomRepo.sortedByType();
                    displayList(sortedList);
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter 1, 2, or 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a number.");
            }
        }
    }

    // ==========================================
    // HELPER & VALIDATION METHODS
    // ==========================================
    
    // Reusable method to ensure correct Room ID format
    private String getValidRoomNumberInput(String promptMessage) {
        String roomNumber;
        while (true) {
            roomNumber = Utilities.getString(promptMessage).trim().toUpperCase();
            if (roomNumber.matches("^R\\d+$")) {
                break;
            }
            System.out.println("Error: Invalid format. Room number must start with 'R' followed by numbers (e.g., R001).");
        }
        return roomNumber;
    }

    // Reusable method to ensure room types fit the clinic categories
    private String getValidRoomType(boolean allowEmpty, String currentValue) {
        String roomType = "";
        boolean isValidType = false;
        
        // Build a display string of allowed types for the prompt
        String typesList = String.join(", ", ALLOWED_ROOM_TYPES);
        String prompt = allowEmpty ? 
            String.format("New Room Type (%s) [%s]: ", typesList, currentValue) : 
            String.format("Enter Room Type (%s): ", typesList);

        while (!isValidType) {
            roomType = Utilities.getString(prompt).trim();
            
            if (allowEmpty && roomType.isEmpty()) {
                return ""; // Skip update
            }

            for (String allowed : ALLOWED_ROOM_TYPES) {
                if (roomType.equalsIgnoreCase(allowed)) {
                    roomType = allowed; // Standardize casing
                    isValidType = true;
                    break;
                }
            }
            
            if (!isValidType) {
                System.out.println("Error: Invalid Room Type. Please choose exactly from the provided list.");
            }
        }
        return roomType;
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

    // ==========================================
    // REPORT GENERATION
    // ==========================================
    public void generateUtilizationReport() {
        String reportText = roomRepo.generateRoomReport(appointmentRepo.getAllAppointments());
        System.out.println(reportText);

        if (!reportText.equals("No room data available to generate report.\n")) {
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = roomRepo.generateRoomHtmlReport(appointmentRepo.getAllAppointments());
                Utilities.exportReportToFile(htmlContent, "RoomUtilizationReport.html");
                System.out.println("SUCCESS: HTML Report saved as 'RoomUtilizationReport.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }

    public void generateAvailabilityDirectory() {
        String reportText = roomRepo.generateAvailabilityDirectoryTextReport();
        System.out.println(reportText);

        if (!reportText.contains("No rooms are currently available.")) {
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = roomRepo.generateAvailabilityDirectoryHtmlReport();
                Utilities.exportReportToFile(htmlContent, "RoomAvailabilityDirectory.html");
                System.out.println("SUCCESS: HTML Report saved as 'RoomAvailabilityDirectory.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }
}