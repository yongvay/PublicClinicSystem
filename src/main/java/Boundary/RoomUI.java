/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundary;

import Control.RoomRepository;
import Control.RoomRepositoryImpl;
import Entity.Room;
import ADT.ListInterface;
import java.util.Scanner;

/**
 * @author Ng Zhi Xuan
 * Boundary class for the Room Subsystem.
 * Handles all user interactions (input/output) for clinic rooms.
 */
public class RoomUI {

    private RoomRepository roomRepo;
    private Scanner scanner;

    // Replace the constructor:
    public RoomUI(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
        this.scanner = new Scanner(System.in);
    }

    // ==========================================
    // MAIN START METHOD
    // ==========================================
    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear bad input
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
            case 0: System.out.println("Exiting Room Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private void addRoom() {
        System.out.println("\n--- Add New Room ---");
        System.out.print("Enter Room Number (e.g., 101): ");
        String roomNumber = scanner.nextLine();
        
        if (roomRepo.findById(roomNumber) != null) {
            System.out.println("Error: Room Number already exists!");
            return;
        }

        System.out.print("Enter Room Type (e.g., Consultation, Ward, ICU): ");
        String roomType = scanner.nextLine();
        
        // By default, a newly added room should be available
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
        System.out.print("\nEnter Room Type to search (e.g., Ward): ");
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

    // ==========================================
    // UTILITY METHODS
    // ==========================================
    private void displayList(ListInterface<Room> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Room r : list) {
            System.out.println(r.toString());
        }
    }
}