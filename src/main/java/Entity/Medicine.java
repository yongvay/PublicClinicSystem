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
    private float price;
    private int reorderLevel; // e.g., if stock drops below 10, trigger a reorder alert

    // Constructor
    public Medicine(String medicineID, String name, String description, String dosage, int quantityInStock, int reorderLevel, float price) {
        this.medicineID = medicineID;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel; // New field
        this.price = price;
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

    public float getPrice() {
        return price;
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

    public void setPrice(float price) {
        this.price = price;
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
        Medicine medicine = (Medicine) obj;
        // Two medicines are considered equal if they have the same Medicine ID
        return medicineID != null && medicineID.equals(medicine.medicineID);
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Desc: %s | Dosage: %s | Stock: %d | Price: RM%.2f",
                medicineID, name, description, dosage, quantityInStock, price);
    }
}
