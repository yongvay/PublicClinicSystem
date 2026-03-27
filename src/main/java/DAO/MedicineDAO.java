package DAO;

import ADT.List;
import ADT.ListInterface;
import Entity.Medicine;
import java.io.*;

/**
 * @author Ng Yong Vay
 * Data Access Object for persisting Medicine records to a text file.
 */
public class MedicineDAO {
    
    // The text file where data will be stored
    private static final String FILE_NAME = "src\\main\\java\\Database\\medicines.txt";
    private static final String DELIMITER = "\\|"; // Regex for splitting
    private static final String SEPARATOR = "|";   // String for joining

    /**
     * Saves the entire custom list to the text file.
     */
    public void saveToFile(ListInterface<Medicine> medicineList) {
        // Using try-with-resources to ensure the file writer closes automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            
            for (Medicine m : medicineList) {
                // Construct a single delimited string line
                String line = m.getMedicineID() + SEPARATOR +
                              m.getName() + SEPARATOR +
                              m.getDescription() + SEPARATOR +
                              m.getDosage() + SEPARATOR +
                              m.getQuantityInStock() + SEPARATOR +
                              m.getReorderLevel();
                
                writer.write(line);
                writer.newLine(); // Move to the next line
            }
        } catch (IOException e) {
            System.err.println("Critical Error: Unable to save data to file -> " + e.getMessage());
        }
    }

    /**
     * Reads the text file and populates a new custom list.
     */
    public ListInterface<Medicine> loadFromFile() {
        ListInterface<Medicine> loadedList = new List<>();
        File file = new File(FILE_NAME);
        
        // If it's the first time running and no file exists, return the empty list
        if (!file.exists()) {
            return loadedList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                
                // Ensure the line has exactly 7 parts before parsing to avoid out-of-bounds errors
                if (parts.length == 7) {
                    Medicine m = new Medicine(
                        parts[0], 
                        parts[1], 
                        parts[2], 
                        parts[3], 
                        Integer.parseInt(parts[4]), // Quantity in Stock
                        Integer.parseInt(parts[5]) // Reorder Level
                    );
                    loadedList.add(m);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Critical Error: File corruption or read failure -> " + e.getMessage());
        }
        
        return loadedList;
    }
}