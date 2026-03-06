package DAO;

import ADT.List;
import ADT.ListInterface;
import Entity.Doctor;
import java.io.*;

/**
 * @author Xing Szen
 * Data Access Object for persisting Doctor records to a text file.
 */
public class DoctorDAO {
    
    // The text file where data will be stored
    private static final String FILE_NAME = "src\\main\\java\\Database\\doctors.txt";
    private static final String DELIMITER = "\\|"; // Regex for splitting
    private static final String SEPARATOR = "|";   // String for joining

    /**
     * Saves the entire custom list to the text file.
     */
    public void saveToFile(ListInterface<Doctor> doctorList) {
        // Using try-with-resources to ensure the file writer closes automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            
            for (Doctor d : doctorList) {
                // Construct a single delimited string line matching the Doctor attributes
                String line = d.getDoctorID() + SEPARATOR +
                              d.getName() + SEPARATOR +
                              d.getSpecialization() + SEPARATOR +
                              d.getContactNum() + SEPARATOR +
                              d.getStatus(); // boolean gets converted to "true" or "false"
                
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
    public ListInterface<Doctor> loadFromFile() {
        ListInterface<Doctor> loadedList = new List<>();
        File file = new File(FILE_NAME);
        
        // If it's the first time running and no file exists, return the empty list
        if (!file.exists()) {
            return loadedList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                
                // Ensure the line has exactly 5 parts (matching Doctor attributes)
                if (parts.length == 5) {
                    Doctor d = new Doctor(
                        parts[0], // Doctor ID
                        parts[1], // Name
                        parts[2], // Specialization
                        parts[3], // Contact Number
                        Boolean.parseBoolean(parts[4]) // Status (Available = true/false)
                    );
                    loadedList.add(d);
                }
            }
        } catch (IOException e) {
            System.err.println("Critical Error: File corruption or read failure -> " + e.getMessage());
        }
        
        return loadedList;
    }
}