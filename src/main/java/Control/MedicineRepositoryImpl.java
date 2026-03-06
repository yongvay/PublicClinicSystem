package Control;

import ADT.List;
import ADT.ListInterface;
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

    public MedicineRepositoryImpl() {
        // Initialize with your custom ADT
        this.medicineList = new List<>();
    }

    // ==========================================
    // CREATE
    // ==========================================
    @Override
    public void create(Medicine medicine) {
        if (medicine != null) {
            medicineList.add(medicine);
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

        // Find the 1-based index of the medicine with the same ID
        for (int i = 1; i <= medicineList.getNumberOfEntries(); i++) {
            Medicine current = medicineList.getEntry(i);
            
            // Updated to match your Entity's getMedicineID()
            if (current.getMedicineID().equalsIgnoreCase(updatedMedicine.getMedicineID())) {
                // Use your custom ADT's replace method (1-based index)
                return medicineList.replace(i, updatedMedicine);
            }
        }
        return false; // Medicine not found
    }

    // ==========================================
    // DELETE
    // ==========================================
    @Override
    public boolean delete(Medicine medicine) {
        if (medicine == null) return false;
        
        // This works perfectly now because you overrode equals() in Medicine.java
        return medicineList.remove(medicine);
    }

    // ==========================================
    // SORTING
    // ==========================================
    @Override
    public ListInterface<Medicine> findAllSortedByName() {
        // Passing a custom Comparator to your ADT's merge sort
        return medicineList.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return m1.getName().compareToIgnoreCase(m2.getName());
            }
        });
    }

    @Override
    public ListInterface<Medicine> findAllSortedByStock() {
        return medicineList.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return Integer.compare(m1.getQuantityInStock(), m2.getQuantityInStock());
            }
        });
    }

    @Override
    public ListInterface<Medicine> findLowStockSorted() {
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