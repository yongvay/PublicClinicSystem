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
    private static final String LIST_SEPARATOR = ";";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void saveToFile(ListInterface<Patient> patientList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Patient p : patientList) {
                String line =
                        p.getPatientID() + SEPARATOR +
                        p.getPatientName() + SEPARATOR +
                        p.getBirthDate().format(formatter) + SEPARATOR +
                        listToString(p.getMedicalHistory()) + SEPARATOR +
                        listToString(p.getAllergies());

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error saving patient data: " + e.getMessage());
        }
    }

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
                            stringToList(parts[3]),
                            stringToList(parts[4])
                    );

                    patientList.add(p);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading patient data: " + e.getMessage());
        }

        return patientList;
    }

    private String listToString(ListInterface<String> list) {
        if (list == null || list.getNumberOfEntries() == 0) {
            return "None";
        }

        ListInterface<String> cleaned = cleanList(list);

        if (cleaned.getNumberOfEntries() == 0) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= cleaned.getNumberOfEntries(); i++) {
            sb.append(cleaned.getEntry(i));

            if (i < cleaned.getNumberOfEntries()) {
                sb.append(LIST_SEPARATOR);
            }
        }

        return sb.toString();
    }

    private ListInterface<String> cleanList(ListInterface<String> list) {
        ListInterface<String> result = new List<>();

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            String item = list.getEntry(i);

            if (item == null) continue;

            item = item.trim();

            if (item.isEmpty()) continue;

            // Normalize Text Format
            item = item.substring(0, 1).toUpperCase() +
                   item.substring(1).toLowerCase();

            // Ignore and Remove duplicate
            boolean exists = false;
            for (int j = 1; j <= result.getNumberOfEntries(); j++) {
                if (result.getEntry(j).equalsIgnoreCase(item)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                result.add(item);
            }
        }

        return result;
    }
    
    private ListInterface<String> stringToList(String data) {
        ListInterface<String> list = new List<>();

        if (data == null || data.equalsIgnoreCase("None") || data.isEmpty()) {
            return list;
        }

        String[] items = data.split(LIST_SEPARATOR);

        for (String item : items) {
            item = item.trim();

            if (!item.isEmpty()) {
                list.add(item); 
            }
        }

        return list;
    }    
}