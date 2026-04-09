package Boundary;

import Control.MedicineRepository;
import Entity.Medicine;
import Utility.Utilities;
import ADT.ListInterface;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * @author Ng Yong Vay
 *         Boundary class for the Medicine Subsystem.
 *         Handles all user interactions (input/output).
 */
public class MedicineUI {

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
        System.out.println("9. Generate Medicine Inventory Report");
        System.out.println("10. Generate Medicine Expiry Report");
        System.out.println("0. Exit to Main Menu");
        System.out.println("==========================================");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1:
                addMedicine();
                break;
            case 2:
                viewAllMedicines();
                break;
            case 3:
                searchById();
                break;
            case 4:
                searchByName();
                break;
            case 5:
                viewLowStockAlerts();
                break;
            case 6:
                updateMedicine();
                break;
            case 7:
                deleteMedicine();
                break;
            case 8:
                viewSortedMedicines();
                break;
            case 9:
                generateMedicineReport();
                break;
            case 10:
                generateExpiryReport();
                break;
            case 0:
                System.out.println("Exiting Medicine Subsystem...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
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

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Description: ");
        String desc = scanner.nextLine();

        System.out.print("Enter Dosage (e.g., 500mg tablet): ");
        String dosage = scanner.nextLine();

        int stock = Utilities.getInt("Enter Initial Stock Quantity: ");
        int reorderLevel = Utilities.getInt("Enter Reorder Level: ");

        LocalDate expiryDate = null;
        boolean validDate = false;

        // Basic input validation to ensure correct date format
        while (!validDate) {
            System.out.print("Enter Expiry Date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine().trim();

            try {
                expiryDate = LocalDate.parse(dateInput);

                if (expiryDate.isBefore(LocalDate.now())) {
                    System.out.println(
                            "⚠️ Warning: The date entered has already passed. You are adding EXPIRED medicine!");
                    System.out.print("Are you sure you want to proceed? (Y/N): ");
                    String confirm = scanner.nextLine().trim();
                    if (!confirm.equalsIgnoreCase("Y")) {
                        continue; // Restarts the while loop to ask for the date again
                    }
                }

                validDate = true; // Breaks the loop if parsing was successful

            } catch (DateTimeParseException e) {
                // Catches the error so the program doesn't crash
                System.out.println("❌ Invalid format! Please use exactly YYYY-MM-DD (e.g., 2026-10-15).");
            }
        }

        Medicine newMed = new Medicine(id, name, desc, dosage, stock, reorderLevel, expiryDate);

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
        Utilities.printHeader("Update Medicine Details");
        String id = Utilities.getString("Enter Medicine ID to update: ");
        Medicine existing = medicineRepo.findById(id);

        if (existing == null) {
            System.out.println("Error: Medicine not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        // 1. Update Name
        updateIfNotEmpty(Utilities.getString("New Name [" + existing.getName() + "]: "), existing::setName);

        // 2. Update Description
        updateIfNotEmpty(Utilities.getString("New Description [" + existing.getDescription() + "]: "),
                existing::setDescription);

        // 3. Update Dosage
        updateIfNotEmpty(Utilities.getString("New Dosage [" + existing.getDosage() + "]: "), existing::setDosage);

        // 4. Update Stock Quantity
        String newStock = Utilities.getString("New Stock Quantity [" + existing.getQuantityInStock() + "]: ");
        if (!newStock.isEmpty()) {
            try {
                existing.setQuantityInStock(Integer.parseInt(newStock));
            } catch (NumberFormatException e) {
                System.out.println("Invalid stock format. Skipping stock update.");
            }
        }

        // 5. Update Reorder Level
        String newReorder = Utilities.getString("New Reorder Level [" + existing.getReorderLevel() + "]: ");
        if (!newReorder.isEmpty()) {
            try {
                existing.setReorderLevel(Integer.parseInt(newReorder));
            } catch (NumberFormatException e) {
                System.out.println("Invalid reorder level format. Skipping reorder level update.");
            }
        }

        System.out.println("Current Expiry Date: " + existing.getExpiryDate());

        boolean validDate = false;
        while (!validDate) {
            System.out.print("Enter New Expiry Date (YYYY-MM-DD) or press [Enter] to keep current: ");
            String dateInput = scanner.nextLine().trim();

            if (dateInput.isEmpty()) {
                System.out.println("No changes made to Expiry Date.");
                validDate = true; // Break the loop, leave the existing date intact
            }
            else {
                try {
                    LocalDate newExpiryDate = LocalDate.parse(dateInput);
                    if (newExpiryDate.isBefore(LocalDate.now())) {
                        System.out.println("⚠️ Warning: You are changing this to an EXPIRED date!");
                        System.out.print("Proceed anyway? (Y/N): ");
                        String confirm = scanner.nextLine().trim();
                        if (!confirm.equalsIgnoreCase("Y")) {
                            continue; // Restarts the loop
                        }
                    }
                    existing.setExpiryDate(newExpiryDate);
                    System.out.println("✅ Expiry Date updated to: " + newExpiryDate);
                    validDate = true;

                } catch (DateTimeParseException e) {
                    System.out.println("❌ Invalid format! Please use exactly YYYY-MM-DD (e.g., 2026-10-15).");
                }
            }
        }

        // Save the updates
        if (medicineRepo.update(existing)) {
            System.out.println("Success: Medicine updated successfully!");
        } else {
            System.out.println("Failed to update medicine.");
        }
    }

    // A functional interface helper to clean up the "if not empty, set" logic
    private void updateIfNotEmpty(String input, java.util.function.Consumer<String> setter) {
        if (!input.isEmpty()) {
            setter.accept(input);
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
                sortedList = medicineRepo.sortedByName();
            } else if (choice == 2) {
                sortedList = medicineRepo.sortedByStock();
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

    private void generateMedicineReport() {
        // 1. Output Text Version to Console First
        String textReport = medicineRepo.generateInventoryTextReport();
        System.out.println(textReport);

        if (!textReport.contains("No medicine data available")) {
            // 2. Ask User if they want the HTML Version
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = medicineRepo.generateInventoryHtmlReport();
                Utilities.exportReportToFile(htmlContent, "MedicineInventoryReport.html");
                System.out.println("SUCCESS: HTML Report saved as 'MedicineInventoryReport.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }

    private void generateExpiryReport() {
        // 1. Output Text Version to Console First
        String textReport = medicineRepo.generateExpiryTextReport();
        System.out.println(textReport);

        if (!textReport.contains("No medicine data available")) {
            // 2. Ask User if they want the HTML Version
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = medicineRepo.generateExpiryHtmlReport();
                Utilities.exportReportToFile(htmlContent, "MedicineExpiryReport.html");
                System.out.println("SUCCESS: HTML Report saved as 'MedicineExpiryReport.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }
    
    // ==========================================
    // UTILITY METHODS
    // ==========================================

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