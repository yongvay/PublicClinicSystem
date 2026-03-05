package Control;

import ADT.List;
import ADT.ListInterface;
import Entity.Medicine;

/**
 * @author Ng Yong Vay
 */
public class MedicineRepositoryImpl {
    
    // As per best practices, we declare the variable using the interface type.
    private ListInterface<Medicine> medicineList;

    public MedicineRepositoryImpl() {
        // Initialize with our custom ADT implementation
        this.medicineList = new List<>();
    }

    /**
     * Adds a new medicine to the system.
     * Prevents duplicate entries based on the Medicine ID.
     */
    public boolean addNewMedicine(Medicine newMedicine) {
        if (searchMedicine(newMedicine.getMedicineID()) != null) {
            return false; 
        }
        return medicineList.add(newMedicine);
    }

    /**
     * Dispenses a specific quantity of medicine, reducing the stock.
     */
    public boolean dispenseMedicine(String medicineID, int quantityToDispense) {
        Medicine med = searchMedicine(medicineID);
        
        if (med != null && med.getQuantityInStock() >= quantityToDispense) {
            med.setQuantityInStock(med.getQuantityInStock() - quantityToDispense);
            return true;
        }
        return false;
    }

    /**
     * Restocks a specific medicine by adding to its current quantity.
     */
    public boolean restockMedicine(String medicineID, int quantityToAdd) {
        Medicine med = searchMedicine(medicineID);
        
        if (med != null && quantityToAdd > 0) {
            med.setQuantityInStock(med.getQuantityInStock() + quantityToAdd);
            return true;
        }
        return false;
    }

    /**
     * Searches for a medicine by its unique ID.
     */
    public Medicine searchMedicine(String medicineID) {
        for (Medicine med : medicineList) {
            if (med.getMedicineID().equalsIgnoreCase(medicineID)) {
                return med;
            }
        }
        return null; // Not found
    }

    /**
     * Generates a formatted string of all medicines currently in the system.
     * NEW FUNCTION ADDED HERE.
     */
    public String getAllMedicinesReport() {
        if (medicineList.isEmpty()) {
            return "No medicines currently available in the system.\n";
        }

        StringBuilder report = new StringBuilder();
        report.append("\n=== ALL MEDICINES LIST ===\n");
        
        for (Medicine med : medicineList) {
            // Relies on the overridden toString() method in your Entity class
            report.append(med.toString()).append("\n");
        }
        
        return report.toString();
    }

    /**
     * Generates a formatted report of all medicines whose stock is at or below the threshold.
     */
    public String generateLowStockReport(int threshold) {
        StringBuilder report = new StringBuilder();
        report.append("=== LOW STOCK REPORT (Threshold: ").append(threshold).append(") ===\n");
        
        boolean foundLowStock = false;
        
        for (Medicine med : medicineList) {
            if (med.getQuantityInStock() <= threshold) {
                report.append(med.toString()).append("\n");
                foundLowStock = true;
            }
        }
        
        if (!foundLowStock) {
            report.append("All medicines are sufficiently stocked.\n");
        }
        
        return report.toString();
    }
}