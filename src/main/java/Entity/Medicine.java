package Entity;

/**
 *
 * @author Ng Yong Vay
 */
public class Medicine {

    private String medicineID;
    private String name;
    private String description;
    private String dosage;  // e.g., "tablet", "mg", "ml", "capsule"
    private int quantityInStock;
    private int reorderLevel; // e.g., if stock drops below 10, trigger a reorder alert

    // For search dummy only
    // This allows creating "dummy" objects for searching using only an ID.
    public Medicine(String medicineID) {
        this.medicineID = medicineID;
    }
    
    // Constructor
    public Medicine(String medicineID, String name, String description, String dosage, int quantityInStock, int reorderLevel) {
        this.medicineID = medicineID;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
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
        return String.format("ID: %s | Name: %s | Desc: %s | Dosage: %s | Stock: %d",
                medicineID, name, description, dosage, quantityInStock);
    }
}
