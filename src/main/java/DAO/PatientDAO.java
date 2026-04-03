package DAO;

import ADT.List;
import ADT.ListInterface;
import Entity.Patient;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 * @author Tam Wan Jin
 */
public class PatientDAO {

    private static final String FILE_NAME = "src\\main\\java\\Database\\patients.txt";
    private static final String DELIMITER = "\\|";
    private static final String SEPARATOR = "|";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
// SAVE FILE
    public void saveToFile(ListInterface<Patient> patientList) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Patient p : patientList) {

                String line =
                        p.getPatientID() + SEPARATOR +
                        p.getPatientName() + SEPARATOR +
                        p.getBirthDate().format(formatter) + SEPARATOR +
                        p.getMedicalHistory() + SEPARATOR +
                        p.getAllergies();

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patient data: " + e.getMessage());
        }
    }

    // LOAD FILE
    public ListInterface<Patient> loadFromFile() {
        ListInterface<Patient> patientList = new List<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return patientList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length == 5) {
                    Patient p = new Patient(
                            parts[0],
                            parts[1],
                            LocalDate.parse(parts[2], formatter),
                            parts[3],
                            parts[4]
                    );
                    patientList.add(p);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading patient data: " + e.getMessage());
        }
        return patientList;
    }
}