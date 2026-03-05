package Boundary;

import Control.MedicineRepository;
import Entity.Medicine;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * @author Ng Yong Vay
 */
public class MedicineUI {
    
    private MedicineRepository manager;
    private Scanner scanner;

    public MedicineUI(MedicineRepository manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    /**
     * The main interactive menu for the Medicine module.
     */
    public void displayMedicineMenu() {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n=========================================");
            System.out.println("   TARUMT Clinic: Medicine Management    ");
            System.out.println("=========================================");
            System.out.println("1. Add New Medicine");
            System.out.println("2. Dispense Medicine");
            System.out.println("3. Restock Medicine");
            System.out.println("4. Show Low Stock Alerts");
            System.out.println("5. View All Medicines");  // <-- NEW OPTION ADDED HERE
            System.out.println("0. Exit to Main Menu");
            System.out.print("Enter choice: ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        promptForNewMedicineDetails();
                        break;
                    case 2:
                        promptForDispense();
                        break;
                    case 3:
                        promptForRestock();
                        break;
                    case 4:
                        showLowStockAlerts();
                        break;
                    case 5:                 // <-- NEW CASE ADDED HERE
                        showAllMedicines(); 
                        break;
                    case 0:
                        System.out.println("Exiting Medicine Menu...");
                        break;
                    default:
                        System.out.println("Error: Invalid choice. Please select from 0-5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the bad input to prevent an infinite loop
            }
        }
    }

    /**
     * Prompts the user for all details required to create a new Medicine entity.
     */
    public void promptForNewMedicineDetails() {
        System.out.println("\n--- Add New Medicine ---");
        try {
            System.out.print("Enter Medicine ID (e.g., M001): ");
            String id = scanner.nextLine().trim();
            
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine().trim();
            
            System.out.print("Enter Dosage (mg): ");
            String dosage = scanner.nextLine().trim();
            
            System.out.print("Enter Initial Stock Quantity: ");
            int stock = scanner.nextInt();
            
            System.out.print("Enter Price (RM): ");
            float price = scanner.nextFloat();
            scanner.nextLine(); // Consume newline

            Medicine newMed = new Medicine(id, name, desc, dosage, stock, price);
            
            if (manager.addNewMedicine(newMed)) {
                System.out.println("Success: Medicine '" + name + "' added successfully.");
            } else {
                System.out.println("Error: A medicine with ID '" + id + "' already exists in the system.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: Invalid numeric input. Medicine addition cancelled.");
            scanner.nextLine(); // Clear bad input
        }
    }

    /**
     * Prompts user to dispense a certain quantity of a medicine.
     */
    private void promptForDispense() {
        System.out.println("\n--- Dispense Medicine ---");
        try {
            System.out.print("Enter Medicine ID to dispense: ");
            String id = scanner.nextLine().trim();
            
            System.out.print("Enter quantity to dispense: ");
            int qty = scanner.nextInt();
            scanner.nextLine();

            if (manager.dispenseMedicine(id, qty)) {
                System.out.println("Success: Medicine dispensed.");
            } else {
                System.out.println("Error: Insufficient stock or invalid Medicine ID.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: Invalid quantity entered.");
            scanner.nextLine();
        }
    }

    /**
     * Prompts user to restock a medicine.
     */
    private void promptForRestock() {
        System.out.println("\n--- Restock Medicine ---");
        try {
            System.out.print("Enter Medicine ID to restock: ");
            String id = scanner.nextLine().trim();
            
            System.out.print("Enter quantity to add: ");
            int qty = scanner.nextInt();
            scanner.nextLine();

            if (manager.restockMedicine(id, qty)) {
                System.out.println("Success: Medicine stock updated.");
            } else {
                System.out.println("Error: Invalid Medicine ID or quantity must be greater than 0.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: Invalid quantity entered.");
            scanner.nextLine();
        }
    }

    /**
     * Prompts the user for a stock threshold and displays the generated report.
     */
    public void showLowStockAlerts() {
        System.out.println("\n--- Low Stock Alerts ---");
        try {
            System.out.print("Enter stock threshold for alert (e.g., 20): ");
            int threshold = scanner.nextInt();
            scanner.nextLine();

            String report = manager.generateLowStockReport(threshold);
            System.out.println("\n" + report);
            
        } catch (InputMismatchException e) {
            System.out.println("Error: Invalid threshold entered.");
            scanner.nextLine();
        }
    }

    /**
     * Fetches and displays the list of all medicines in the system.
     * NEW FUNCTION ADDED HERE.
     */
    public void showAllMedicines() {
        String report = manager.getAllMedicinesReport();
        System.out.println(report);
    }
}