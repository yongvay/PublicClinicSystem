package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.MedicineDAO;
import Entity.Medicine;
import java.util.Comparator;

/**
 * @author Ng Yong Vay
 * Implementation of the MedicineRepository using a custom List ADT.
 * Acts as the in-memory database for Medicine entities.
 */
public class MedicineRepositoryImpl implements MedicineRepository {

    // Coding to an Interface (CLO2 best practice)
    private ListInterface<Medicine> medicineList;
    private MedicineDAO medicineDAO; // Instantiate the DAO

    public MedicineRepositoryImpl() {
        this.medicineDAO = new MedicineDAO();
        
        // Load existing data from file 
        this.medicineList = medicineDAO.loadFromFile();
    }

    // ==========================================
    // AUTO-GENERATE ID
    // ==========================================
    @Override
    public String generateNextMedicineId() {
        int maxId = 0;
        
        // Loop through the custom Iterable List to find the highest ID
        for (Medicine m : medicineList) {
            String currentIdStr = m.getMedicineID();
            
            // Check if the ID starts with "M" to safely parse the number
            if (currentIdStr != null && currentIdStr.startsWith("M")) {
                try {
                    // Extract the numeric part (e.g., "001" from "M001")
                    int currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore any badly formatted IDs
                }
            }
        }
        
        // Add 1 to the max ID found, and format it back to "M" + 3 digits (e.g., M005)
        return String.format("M%03d", maxId + 1);
    }
    
    // ==========================================
    // CREATE
    // ==========================================
    @Override
    public void create(Medicine medicine) {
        if (medicine != null) {
            medicineList.add(medicine);
            medicineDAO.saveToFile(medicineList); // Save after adding
        }
    }

    // ==========================================
    // READ
    // ==========================================
    @Override
    public ListInterface<Medicine> findAll() {
        return medicineList;
    }

    @Override
    public Medicine findById(String id) {
        if (id == null) return null;
        
        // Using the Iterable feature of your custom list
        for (Medicine m : medicineList) {
            // Updated to match your Entity's getMedicineID()
            if (m.getMedicineID().equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public ListInterface<Medicine> findByName(String name) {
        ListInterface<Medicine> results = new List<>();
        if (name == null || name.trim().isEmpty()) return results;

        String searchLower = name.toLowerCase();
        for (Medicine m : medicineList) {
            if (m.getName().toLowerCase().contains(searchLower)) {
                results.add(m);
            }
        }
        return results;
    }

    @Override
    public ListInterface<Medicine> findOutOfStock() {
        ListInterface<Medicine> results = new List<>();
        for (Medicine m : medicineList) {
            // Updated to match your Entity's getQuantityInStock()
            if (m.getQuantityInStock() == 0) {
                results.add(m);
            }
        }
        return results;
    }

    @Override
    public ListInterface<Medicine> findBelowReorderLevel() {
        ListInterface<Medicine> results = new List<>();
        for (Medicine m : medicineList) {
            // Utilizing the newly added reorderLevel logic
            if (m.getQuantityInStock() < m.getReorderLevel()) {
                results.add(m);
            }
        }
        return results;
    }

    // ==========================================
    // UPDATE
    // ==========================================
    @Override
    public boolean update(Medicine updatedMedicine) {
        if (updatedMedicine == null) return false;

        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine current = medicineList.getEntry(i);
            
            if (current.getMedicineID().equalsIgnoreCase(updatedMedicine.getMedicineID())) {
                boolean success = medicineList.replace(i, updatedMedicine);
                if (success) {
                    medicineDAO.saveToFile(medicineList); // Save after updating
                }
                return success;
            }
        }
        return false; 
    }

    // ==========================================
    // DELETE
    // ==========================================
    @Override
    public boolean delete(Medicine medicine) {
        if (medicine == null) return false;
        
        boolean success = medicineList.remove(medicine);
        if (success) {
            medicineDAO.saveToFile(medicineList); // Save after deleting
        }
        return success;
    }

    // ==========================================
    // SORTING
    // ==========================================
    @Override
    public ListInterface<Medicine> sortedByName() {
        // Passing a custom Comparator to your ADT's merge sort
        return medicineList.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return m1.getName().compareToIgnoreCase(m2.getName());
            }
        });
    }

    @Override
    public ListInterface<Medicine> sortedByStock() {
        return medicineList.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return Integer.compare(m1.getQuantityInStock(), m2.getQuantityInStock());
            }
        });
    }

    @Override
    public ListInterface<Medicine> lowStockSorted() {
        // First, filter the list to only those below reorder level
        ListInterface<Medicine> lowStock = findBelowReorderLevel();
        
        // Then sort the filtered list by stock (ascending)
        return lowStock.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return Integer.compare(m1.getQuantityInStock(), m2.getQuantityInStock());
            }
        });
    }
}