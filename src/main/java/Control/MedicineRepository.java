package Control;

/**
 * @author Ng Yong Vay
 */
 
import ADT.ListInterface;
import Entity.Medicine;

public interface MedicineRepository {

  String generateNextMedicineId();
    
  // Create
  void create(Medicine medicine);

  // Read
  ListInterface<Medicine> findAll();
  Medicine findById(String id); // Not used but implemented
  ListInterface<Medicine> findByName(String name);
  ListInterface<Medicine> findOutOfStock();
  ListInterface<Medicine> findBelowReorderLevel();

  // Update
  boolean update(Medicine medicine);

  // Delete
  boolean delete(Medicine medicine);
  
  // Sorting methods
  ListInterface<Medicine> sortedByName();
  ListInterface<Medicine> sortedByStock();
  ListInterface<Medicine> lowStockSorted(); //Not used

  // Reporting methods
  String generateInventoryReport();
}