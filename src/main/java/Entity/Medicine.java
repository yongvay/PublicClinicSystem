package Entity;

import java.time.LocalDate;

/**
 *
 * @author Ng Yong Vay
 */
public class Medicine {

    private String medicineID;
    private String name;
    private String description;
    private String dosage; // e.g., "tablet", "mg", "ml", "capsule"
    private int quantityInStock;
    private int reorderLevel; // e.g., if stock drops below 10, trigger a reorder alert
    private LocalDate expiryDate;

    // Used to create a temporary "search key" or "dummy object" to efficiently find, replace, or remove items from your ADT List based purely on the ID.
    public Medicine(String medicineID) {
        this.medicineID = medicineID;
    }

    // Used to create actual data records with all attributes for storage and manipulation.
    public Medicine(String medicineID, String name, String description, String dosage, int quantityInStock,
            int reorderLevel, LocalDate expiryDate) {
        this.medicineID = medicineID;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public String getMedicineID() {
        return medicineID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDosage() {
        return dosage;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    // ==========================================
    // SETTERS
    // ==========================================
    public void setMedicineID(String medicineID) {
        this.medicineID = medicineID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // ==========================================
    // OVERRIDDEN METHODS
    // ==========================================
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Medicine other = (Medicine) obj;

        // Use equalsIgnoreCase for case-insensitive ID comparison.
        // This ensures "M001" matches "m001" during ADT searches.
        return this.medicineID != null && this.medicineID.equalsIgnoreCase(other.medicineID);
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Desc: %s | Dosage: %s | Stock: %d | Reorder: %d | Expiry: %s",
                medicineID, name, description, dosage, quantityInStock, reorderLevel, expiryDate);
    }
}
