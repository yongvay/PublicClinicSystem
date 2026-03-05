package Test;

import Boundary.MedicineUI;
import Control.MedicineManager;
import Entity.Medicine;

/**
 * @author [Insert Your Name Here]
 * Main test class to launch the Medicine module prototype.
 */
public class TestMedicine {

    public static void main(String args[]) {
        
        // 1. Initialize the Control class (The Brain)
        MedicineManager manager = new MedicineManager();
        
        // 2. Pre-load dummy data for easier testing 
        loadDummyData(manager);
        
        // 3. Initialize the Boundary class (The Face) and pass the Control class to it
        MedicineUI ui = new MedicineUI(manager);
        
        // 4. Start the application loop
        ui.displayMedicineMenu();
        
        System.out.println("System terminated safely. Goodbye!");
    }

    /**
     * Helper method to populate the system with initial hard-coded data.
     * This is permitted by the assignment specification for prototypes.
     */
    private static void loadDummyData(MedicineManager manager) {
        System.out.println("System Initialization: Loading dummy medicine data...");
        
        manager.addNewMedicine(new Medicine("M001", "Paracetamol", "Pain reliever and fever reducer", "500f", 100, 1.50f));
        manager.addNewMedicine(new Medicine("M002", "Amoxicillin", "Antibiotic for bacterial infections", "250f", 50, 5.00f));
        manager.addNewMedicine(new Medicine("M003", "Ibuprofen", "Nonsteroidal anti-inflammatory drug", "400f", 15, 3.20f)); // Low stock example
        manager.addNewMedicine(new Medicine("M004", "Loratadine", "Antihistamine for allergies", "10f", 200, 2.00f));
        manager.addNewMedicine(new Medicine("M005", "Omeprazole", "Reduces stomach acid", "20f", 8, 4.50f)); // Low stock example
        
        System.out.println("System Initialization: Complete.\n");
    }
    }