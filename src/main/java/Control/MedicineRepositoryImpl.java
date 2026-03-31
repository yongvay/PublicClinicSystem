package Control;

import ADT.List;
import ADT.ListInterface;
import ADT.SearchCriteria;
import DAO.MedicineDAO;
import Entity.Medicine;
import java.util.Comparator;

/**
 * @author Ng Yong Vay
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

    // Auto Generate Medicine ID
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
    public Medicine findById(final String id) {
        if (id == null)
            return null;

        // Centralized single-search
        return medicineList.findFirst(new SearchCriteria<Medicine>() {
            @Override
            public boolean isMatch(Medicine m) {
                return m.getMedicineID().equalsIgnoreCase(id);
            }
        });
    }

    @Override
    public ListInterface<Medicine> findByName(String name) {
        if (name == null || name.trim().isEmpty())
            return new List<>();

        final String searchLower = name.toLowerCase();

        // Centralized multi-search
        return medicineList.findAll(new SearchCriteria<Medicine>() {
            @Override
            public boolean isMatch(Medicine m) {
                return m.getName().toLowerCase().contains(searchLower);
            }
        });
    }

    @Override
    public ListInterface<Medicine> findOutOfStock() {
        return medicineList.findAll(new SearchCriteria<Medicine>() {
            @Override
            public boolean isMatch(Medicine m) {
                return m.getQuantityInStock() == 0;
            }
        });
    }

    @Override
    public ListInterface<Medicine> findBelowReorderLevel() {
        return medicineList.findAll(new SearchCriteria<Medicine>() {
            @Override
            public boolean isMatch(Medicine m) {
                return m.getQuantityInStock() < m.getReorderLevel();
            }
        });
    }

    // ==========================================
    // UPDATE
    // ==========================================
    @Override
    public boolean update(Medicine updatedMedicine) {
        if (updatedMedicine == null)
            return false;

        int position = medicineList.getPosition(updatedMedicine);

        if (position != -1) {
            boolean success = medicineList.replace(position, updatedMedicine);

            if (success) {
                medicineDAO.saveToFile(medicineList);
            }
            return success;
        }
        return false;
    }

    // ==========================================
    // DELETE
    // ==========================================
    @Override
    public boolean delete(Medicine medicine) {
        if (medicine == null)
            return false;

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