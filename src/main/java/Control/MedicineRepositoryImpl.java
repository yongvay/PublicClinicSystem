package Control;

import ADT.List;
import ADT.ListInterface;
import ADT.SearchCriteria;
import DAO.MedicineDAO;
import Entity.Medicine;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author Ng Yong Vay
 */
public class MedicineRepositoryImpl implements MedicineRepository {

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
            medicineDAO.saveToFile(medicineList);
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
        ListInterface<Medicine> lowStock = findBelowReorderLevel();

        // Then sort the filtered list by stock (ascending)
        return lowStock.sort(new Comparator<Medicine>() {
            @Override
            public int compare(Medicine m1, Medicine m2) {
                return Integer.compare(m1.getQuantityInStock(), m2.getQuantityInStock());
            }
        });
    }

    // ==========================================
    // 1. INVENTORY REPORT (TEXT FOR CONSOLE)
    // ==========================================
    @Override
    public String generateInventoryTextReport() {
        ListInterface<Medicine> allMedicines = this.findAll();

        if (allMedicines.isEmpty()) {
            return "No medicine data available to generate report.\n";
        }

        ListInterface<Medicine> lowStockMeds = this.findBelowReorderLevel();
        ListInterface<Medicine> outOfStockMeds = this.findOutOfStock();

        int totalUniqueMedicines = allMedicines.getNumberOfEntries();
        int totalPhysicalStock = 0;
        for (Medicine m : allMedicines)
            totalPhysicalStock += m.getQuantityInStock();

        int outOfStockCount = outOfStockMeds.getNumberOfEntries();
        int lowStockCount = lowStockMeds.getNumberOfEntries();
        int healthyStockCount = totalUniqueMedicines - outOfStockCount - lowStockCount;

        StringBuilder report = new StringBuilder();
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        report.append("\n========================================================================================\n");
        report.append("                          CLINIC MEDICINE INVENTORY & RESTOCK REPORT                    \n");
        report.append("                          Generated At: ").append(time).append("\n");
        report.append("========================================================================================\n");
        report.append("Total Unique Medicine Types  : ").append(totalUniqueMedicines).append("\n");
        report.append("Total Physical Units in Stock: ").append(totalPhysicalStock).append("\n");
        report.append("Healthy: ").append(healthyStockCount).append(" | Low: ").append(lowStockCount).append(" | Out: ")
                .append(outOfStockCount).append("\n");
        report.append("========================================================================================\n");

        ListInterface<Medicine> sortedInventory = this.sortedByStock();
        report.append(String.format("| %-8s | %-20s | %-8s | %-12s | %-8s |\n", "Med ID", "Medicine Name", "Stock",
                "Reorder Lvl", "Status"));
        report.append("----------------------------------------------------------------------------------------\n");
        for (Medicine m : sortedInventory) {
            String status = (m.getQuantityInStock() == 0) ? "OUT"
                    : (m.getQuantityInStock() < m.getReorderLevel()) ? "LOW" : "OK";
            report.append(String.format("| %-8s | %-20s | %-8d | %-12d | %-8s |\n",
                    m.getMedicineID(),
                    (m.getName().length() > 20 ? m.getName().substring(0, 17) + "..." : m.getName()),
                    m.getQuantityInStock(), m.getReorderLevel(), status));
        }
        return report.toString();
    }

    // ==========================================
    // 2. INVENTORY REPORT (HTML FOR EXPORT)
    // ==========================================
    @Override
    public String generateInventoryHtmlReport() {
        ListInterface<Medicine> allMedicines = this.findAll();
        if (allMedicines.isEmpty())
            return "<h1>No Data Available</h1>";

        // UNCOMMENTED: Fetch the lists
        ListInterface<Medicine> lowStockMeds = this.findBelowReorderLevel();
        ListInterface<Medicine> outOfStockMeds = this.findOutOfStock();

        int totalUnique = allMedicines.getNumberOfEntries();
        int totalStock = 0;
        for (Medicine m : allMedicines) {
            totalStock += m.getQuantityInStock();
        }

        // UNCOMMENTED: Calculate the counts
        int outCount = outOfStockMeds.getNumberOfEntries();
        int lowCount = lowStockMeds.getNumberOfEntries();
        int healthyCount = totalUnique - outCount - lowCount;

        StringBuilder html = new StringBuilder();
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Medicine Inventory</title>\n");

        // Original CSS
        html.append(
                "<style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; background: #f8f9fa; } table { width: 100%; border-collapse: collapse; background: white; } th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; } th { background-color: #34495e; color: white; } .out { background-color: #ffeaea; color: #c0392b; font-weight: bold; } .low { background-color: #fff3cd; color: #d35400; font-weight: bold; } .ok { color: #27ae60; }</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<h1>📦 Clinic Medicine Inventory Report</h1>\n<p>Generated: ").append(time).append("</p>\n");

        html.append("<p>Total Unique: <strong>").append(totalUnique).append("</strong> | Total Stock: <strong>")
                .append(totalStock).append("</strong></p>\n");

        // NEW: Injecting the Status Summary to utilize the uncommented data
        html.append("<p>Status Summary: ")
                .append("<span class=\"ok\">Healthy: ").append(healthyCount).append("</span> | ")
                .append("<span class=\"low\">Low: ").append(lowCount).append("</span> | ")
                .append("<span class=\"out\">Out of Stock: ").append(outCount).append("</span></p>\n");

        html.append(
                "<table>\n<tr><th>Med ID</th><th>Medicine Name</th><th>Stock</th><th>Reorder Lvl</th><th>Status</th></tr>\n");

        for (Medicine m : this.sortedByStock()) {
            String rowClass = (m.getQuantityInStock() == 0) ? "out"
                    : (m.getQuantityInStock() < m.getReorderLevel()) ? "low" : "ok";
            String status = (m.getQuantityInStock() == 0) ? "OUT"
                    : (m.getQuantityInStock() < m.getReorderLevel()) ? "LOW" : "OK";
            html.append("<tr class=\"").append(rowClass).append("\"><td>").append(m.getMedicineID()).append("</td><td>")
                    .append(m.getName()).append("</td><td>").append(m.getQuantityInStock()).append("</td><td>")
                    .append(m.getReorderLevel()).append("</td><td>").append(status).append("</td></tr>\n");
        }

        html.append("</table>\n</body>\n</html>");
        return html.toString();
    }

    // ==========================================
    // 3. EXPIRY REPORT (TEXT FOR CONSOLE)
    // ==========================================
    @Override
    public String generateExpiryTextReport() {
        ListInterface<Medicine> allMedicines = findAll();
        if (allMedicines.isEmpty())
            return "No medicine data available.\n";

        int n = allMedicines.getNumberOfEntries();
        Medicine[] sortedMeds = new Medicine[n];
        for (int i = 1; i <= n; i++)
            sortedMeds[i - 1] = allMedicines.getEntry(i);
        for (int i = 1; i < n; i++) {
            Medicine key = sortedMeds[i];
            int j = i - 1;
            while (j >= 0 && sortedMeds[j].getExpiryDate().isAfter(key.getExpiryDate())) {
                sortedMeds[j + 1] = sortedMeds[j];
                j = j - 1;
            }
            sortedMeds[j + 1] = key;
        }

        StringBuilder report = new StringBuilder();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        report.append("\n========================================================================================\n");
        report.append("                          CLINIC MEDICINE EXPIRY & SHELF-LIFE REPORT                    \n");
        report.append("========================================================================================\n");
        report.append(String.format("| %-8s | %-20s | %-8s | %-15s | %-20s |\n", "Med ID", "Medicine Name", "Stock",
                "Expiry Date", "Status"));
        report.append("----------------------------------------------------------------------------------------\n");

        for (Medicine m : sortedMeds) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, m.getExpiryDate());
            String statusText = (daysUntilExpiry < 0) ? "EXPIRED (" + Math.abs(daysUntilExpiry) + "d ago)"
                    : (daysUntilExpiry <= 90) ? "EXPIRING (" + daysUntilExpiry + "d left)" : "SAFE";

            report.append(String.format("| %-8s | %-20s | %-8d | %-15s | %-20s |\n",
                    m.getMedicineID(),
                    (m.getName().length() > 20 ? m.getName().substring(0, 17) + "..." : m.getName()),
                    m.getQuantityInStock(), m.getExpiryDate().format(dtf), statusText));
        }
        return report.toString();
    }

    // ==========================================
    // 4. EXPIRY REPORT (HTML FOR EXPORT)
    // ==========================================
    @Override
    public String generateExpiryHtmlReport() {
        // KEEP YOUR EXACT HTML EXPIRY CODE HERE FROM YOUR UPLOADED FILE
        ListInterface<Medicine> allMedicines = findAll();
        if (allMedicines.isEmpty())
            return "<h1>No Data Available</h1>";

        int n = allMedicines.getNumberOfEntries();
        Medicine[] sortedMeds = new Medicine[n];
        for (int i = 1; i <= n; i++)
            sortedMeds[i - 1] = allMedicines.getEntry(i);
        for (int i = 1; i < n; i++) {
            Medicine key = sortedMeds[i];
            int j = i - 1;
            while (j >= 0 && sortedMeds[j].getExpiryDate().isAfter(key.getExpiryDate())) {
                sortedMeds[j + 1] = sortedMeds[j];
                j = j - 1;
            }
            sortedMeds[j + 1] = key;
        }

        StringBuilder html = new StringBuilder();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        html.append(
                "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Medicine Expiry Report</title>\n");
        html.append(
                "<style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; background: #f8f9fa; } table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); } th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; } th { background-color: #34495e; color: white; } .expired { background-color: #ffeaea; color: #c0392b; font-weight: bold; } .expiring-soon { background-color: #fff3cd; color: #d35400; font-weight: bold; } .safe { color: #27ae60; }</style>\n</head>\n<body>\n");
        html.append("<h1>⚠️ Clinic Medicine Expiry & Shelf-Life Report</h1>\n");
        html.append(
                "<table>\n<tr><th>Med ID</th><th>Medicine Name</th><th>Stock</th><th>Expiry Date</th><th>Status / Days Left</th></tr>\n");

        for (Medicine m : sortedMeds) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, m.getExpiryDate());
            String rowClass = (daysUntilExpiry < 0) ? "expired" : (daysUntilExpiry <= 90) ? "expiring-soon" : "safe";
            String statusText = (daysUntilExpiry < 0) ? "EXPIRED (" + Math.abs(daysUntilExpiry) + " days ago)"
                    : (daysUntilExpiry <= 90) ? "EXPIRING SOON (" + daysUntilExpiry + " days left)" : "SAFE";

            html.append("<tr class=\"").append(rowClass).append("\"><td>").append(m.getMedicineID()).append("</td><td>")
                    .append(m.getName()).append("</td><td>").append(m.getQuantityInStock()).append("</td><td>")
                    .append(m.getExpiryDate().format(dtf)).append("</td><td>").append(statusText)
                    .append("</td></tr>\n");
        }
        html.append("</table>\n</body>\n</html>");
        return html.toString();
    }
}