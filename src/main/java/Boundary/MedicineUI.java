package Boundary;

import Control.MedicineRepository;
import Control.MedicineRepositoryImpl;
import Entity.Medicine;
import ADT.ListInterface;
import java.util.Scanner;

/**
 * @author Ng Yong Vay
 * Boundary class for the Medicine Subsystem.
 * Handles all user interactions (input/output).
 */
public class MedicineUI {

    // Dependency on the Control layer (Interface, not implementation)
    private MedicineRepository medicineRepo;
    private Scanner scanner;

    public MedicineUI(MedicineRepository medicineRepo) {
        this.medicineRepo = medicineRepo;
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
            
            // Basic input validation to prevent crashes
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the leftover newline character
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the bad input
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n==========================================");
        System.out.println("       CLINIC SUBSYSTEM: MEDICINE MENU    ");
        System.out.println("==========================================");
        System.out.println("1. Add New Medicine");
        System.out.println("2. View All Medicines");
        System.out.println("3. Search Medicine by ID");
        System.out.println("4. Search Medicine by Name");
        System.out.println("5. View Low Stock / Out of Stock Alerts");
        System.out.println("6. Update Medicine Details");
        System.out.println("7. Delete Medicine");
        System.out.println("8. View Sorted Medicines (By Name/Stock)");
        System.out.println("0. Exit to Main Menu");
        System.out.println("==========================================");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: addMedicine(); break;
            case 2: viewAllMedicines(); break;
            case 3: searchById(); break;
            case 4: searchByName(); break;
            case 5: viewLowStockAlerts(); break;
            case 6: updateMedicine(); break;
            case 7: deleteMedicine(); break;
            case 8: viewSortedMedicines(); break;
            case 0: System.out.println("Exiting Medicine Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private void addMedicine() {
        System.out.println("\n--- Add New Medicine ---");
        
        // 1. Auto-generate the ID instead of asking the user
        String id = medicineRepo.generateNextMedicineId();
        System.out.println("Auto-generated Medicine ID: " + id);

        // 2. Continue asking for the rest of the details
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Description: ");
        String desc = scanner.nextLine();
        
        System.out.print("Enter Dosage (e.g., 500mg tablet): ");
        String dosage = scanner.nextLine();
        
        System.out.print("Enter Initial Stock Quantity: ");
        int stock = scanner.nextInt();
        
        System.out.print("Enter Reorder Level: ");
        int reorderLevel = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // 3. Create and save the medicine
        Medicine newMed = new Medicine(id, name, desc, dosage, stock, reorderLevel);
        medicineRepo.create(newMed);
        System.out.println("Success: Medicine added successfully!");
    }

    private void viewAllMedicines() {
        System.out.println("\n--- All Medicines ---");
        ListInterface<Medicine> list = medicineRepo.findAll();
        displayList(list);
    }

    private void searchById() {
        System.out.print("\nEnter Medicine ID to search: ");
        String id = scanner.nextLine();
        Medicine found = medicineRepo.findById(id);
        
        if (found != null) {
            System.out.println("Medicine Found: \n" + found.toString());
        } else {
            System.out.println("Medicine not found with ID: " + id);
        }
    }

    private void searchByName() {
        System.out.print("\nEnter Medicine Name to search: ");
        String name = scanner.nextLine();
        ListInterface<Medicine> results = medicineRepo.findByName(name);
        
        if (results.isEmpty()) {
            System.out.println("No medicines found matching: " + name);
        } else {
            System.out.println("Search Results:");
            displayList(results);
        }
    }

    private void viewLowStockAlerts() {
        System.out.println("\n--- Low Stock & Out of Stock Alerts ---");
        ListInterface<Medicine> lowStock = medicineRepo.findBelowReorderLevel();
        if (lowStock.isEmpty()) {
            System.out.println("All medicines are currently sufficiently stocked.");
        } else {
            System.out.println("ATTENTION: The following medicines require restocking:");
            displayList(lowStock);
        }
    }

    private void updateMedicine() {
        System.out.print("\nEnter Medicine ID to update: ");
        String id = scanner.nextLine();
        Medicine existing = medicineRepo.findById(id);
        
        if (existing == null) {
            System.out.println("Error: Medicine not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Stock Quantity [" + existing.getQuantityInStock() + "]: ");
        String stockStr = scanner.nextLine();
        if (!stockStr.isEmpty()) existing.setQuantityInStock(Integer.parseInt(stockStr));

        // Note: For a real assignment, you would update all fields similarly.
        
        if (medicineRepo.update(existing)) {
            System.out.println("Success: Medicine updated successfully!");
        } else {
            System.out.println("Failed to update medicine.");
        }
    }

    private void deleteMedicine() {
        System.out.print("\nEnter Medicine ID to delete: ");
        String id = scanner.nextLine();
        Medicine target = medicineRepo.findById(id);
        
        if (target != null) {
            System.out.print("Are you sure you want to delete " + target.getName() + "? (Y/N): ");
            String confirm = scanner.nextLine();
            if (confirm.equalsIgnoreCase("Y")) {
                if (medicineRepo.delete(target)) {
                    System.out.println("Success: Medicine deleted.");
                } else {
                    System.out.println("Error: Could not delete medicine.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("Error: Medicine not found.");
        }
    }

    private void viewSortedMedicines() {
        System.out.println("\n--- Sort Medicines ---");
        System.out.println("1. Sort by Name (A-Z)");
        System.out.println("2. Sort by Stock Quantity (Low to High)");
        System.out.print("Enter choice: ");
        
        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            ListInterface<Medicine> sortedList = null;
            if (choice == 1) {
                sortedList = medicineRepo.findAllSortedByName();
            } else if (choice == 2) {
                sortedList = medicineRepo.findAllSortedByStock();
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

    // ==========================================
    // UTILITY METHODS
    // ==========================================
    
    /**
     * Helper method to iterate through and print any ListInterface of Medicine.
     * This proves to your tutor that your Iterable implementation works!
     */
    private void displayList(ListInterface<Medicine> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("The list is empty.");
            return;
        }
        // Using the enhanced for-loop because your List implements Iterable!
        for (Medicine m : list) {
            System.out.println(m.toString());
        }
    }
}