
package Control;

/**
 * @author Ng Yong Vay
 */
 
import ADT.ListInterface;
import Entity.Medicine;

public interface MedicineRepository {

  // Create
  void create(Medicine medicine);

  // Read
  ListInterface<Medicine> findAll();
  Medicine findById(String id);
  ListInterface<Medicine> findByName(String name);
  ListInterface<Medicine> findOutOfStock();
  ListInterface<Medicine> findBelowReorderLevel();

  // Update
  boolean update(Medicine medicine);

  // Delete
  boolean delete(Medicine medicine);
  
  // Sorting methods
  ListInterface<Medicine> findAllSortedByName();
  ListInterface<Medicine> findAllSortedByStock();
  ListInterface<Medicine> findLowStockSorted();
}