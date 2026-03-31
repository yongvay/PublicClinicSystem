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

    @Override
    public String generateInventoryReport() {
        ListInterface<Medicine> allMedicines = this.findAll();

        if (allMedicines.isEmpty()) {
            return "No medicine data available to generate report.\n";
        }

        // 1. Fetch ADT Filtered Data
        ListInterface<Medicine> lowStockMeds = this.findBelowReorderLevel();
        ListInterface<Medicine> outOfStockMeds = this.findOutOfStock();

        // 2. Calculate Aggregates
        int totalUniqueMedicines = allMedicines.getNumberOfEntries();
        int totalPhysicalStock = 0;

        for (Medicine m : allMedicines) {
            totalPhysicalStock += m.getQuantityInStock();
        }

        int outOfStockCount = outOfStockMeds.getNumberOfEntries();
        int lowStockCount = lowStockMeds.getNumberOfEntries();
        int healthyStockCount = totalUniqueMedicines - outOfStockCount - lowStockCount;

        // 3. Build the Report String
        StringBuilder report = new StringBuilder();
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        report.append("\n========================================================================================\n");
        report.append("                          CLINIC MEDICINE INVENTORY & RESTOCK REPORT                    \n");
        report.append("                          Generated At: ").append(time).append("\n");
        report.append("========================================================================================\n");

        report.append("\n[1] OVERALL INVENTORY SUMMARY\n");
        report.append("--------------------------------------------------\n");
        report.append("Total Unique Medicine Types  : ").append(totalUniqueMedicines).append("\n");
        report.append("Total Physical Units in Stock: ").append(totalPhysicalStock).append("\n");

        report.append("\n[2] STOCK HEALTH ANALYSIS\n");
        report.append("--------------------------------------------------\n");
        report.append("Healthy Stock (Adequate)     : ").append(healthyStockCount).append("\n");
        report.append("Low Stock (Needs Reorder)    : ").append(lowStockCount).append("\n");
        report.append("Out of Stock (CRITICAL)      : ").append(outOfStockCount).append("\n");

        if (lowStockCount > 0 || outOfStockCount > 0) {
            report.append("\n[3] ACTION REQUIRED: CRITICAL RESTOCK LIST\n");
            report.append("----------------------------------------------------------------------------------------\n");
            report.append(String.format("| %-8s | %-20s | %-15s | %-8s | %-12s |\n",
                    "Med ID", "Medicine Name", "Dosage", "Stock", "Reorder Lvl"));
            report.append("----------------------------------------------------------------------------------------\n");

            for (Medicine m : lowStockMeds) {
                String statusMarker = (m.getQuantityInStock() == 0) ? "**OUT**" : "LOW";
                report.append(String.format("| %-8s | %-20s | %-15s | %-3d %-4s | %-12d |\n",
                        m.getMedicineID(), m.getName(), m.getDosage(), m.getQuantityInStock(), statusMarker,
                        m.getReorderLevel()));
            }
        }

        report.append("\n[4] COMPLETE INVENTORY LOG (Sorted by Stock Level)\n");
        report.append("----------------------------------------------------------------------------------------\n");
        report.append(String.format("| %-8s | %-20s | %-25s | %-8s | %-8s |\n",
                "Med ID", "Medicine Name", "Description", "Stock", "Status"));
        report.append("----------------------------------------------------------------------------------------\n");

        ListInterface<Medicine> sortedInventory = this.sortedByStock();
        for (Medicine m : sortedInventory) {
            String status = (m.getQuantityInStock() == 0) ? "OUT"
                    : (m.getQuantityInStock() < m.getReorderLevel()) ? "LOW" : "OK";

            report.append(String.format("| %-8s | %-20s | %-25s | %-8d | %-8s |\n",
                    m.getMedicineID(),
                    (m.getName().length() > 20 ? m.getName().substring(0, 17) + "..." : m.getName()),
                    (m.getDescription().length() > 25 ? m.getDescription().substring(0, 22) + "..."
                            : m.getDescription()),
                    m.getQuantityInStock(),
                    status));
        }
        report.append("========================================================================================\n");
        report.append("End of Report.\n");

        return report.toString();
    }

}