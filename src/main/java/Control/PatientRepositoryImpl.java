package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.PatientDAO;
import Entity.Patient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * @author Tam Wan Jin
 */
public class PatientRepositoryImpl implements PatientRepository {

    private final ListInterface<Patient> patientList;
    private final PatientDAO patientDAO;

    public PatientRepositoryImpl() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.loadFromFile();
    }
    
    
    // AUTO ID GENERATION
    @Override
    public String generatePatientID() {
        int max = 0;

        if (patientList.isEmpty()) {
            return "P001";
        }

        for (Patient p : patientList) {
            String id = p.getPatientID();
            if (id != null && id.length() > 1) {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) {
                    max = num;
                }
            }
        }

        return "P" + String.format("%03d", max + 1);
    }

    // CREATE
    @Override
    public void create(Patient patient) {
        if (patient != null) {
            patientList.add(patient);
            patientDAO.saveToFile(patientList);
        }
    }
    

    
    // READ
    @Override
    public ListInterface<Patient> findAll() {
        return patientList;
    }

    // UPDATE
    @Override
    public boolean update(Patient updatedPatient) {
        int pos = patientList.getPosition(updatedPatient);

        if (pos != -1) {
            boolean success = patientList.replace(pos, updatedPatient);

            if (success) {
                patientDAO.saveToFile(patientList);
            }
            return success;
        }

        return false;
    }
    
    @Override
    public boolean updatePatientAllergy(String patientId, String oldA, String newA) {

        Patient p = findById(patientId);

        if (p != null && newA != null && !newA.trim().isEmpty()) {

            ListInterface<String> list = p.getAllergies();

            int pos = list.getPosition(oldA);

            if (pos != -1) {
                boolean success = list.replace(pos, newA);

                if (success) {
                    patientDAO.saveToFile(patientList);
                }

                return success;
            }
        }
        return false;
    }    

    @Override
    public boolean updatePatientMedicalHistory(String patientId, String oldH, String newH) {

        Patient p = findById(patientId);

        if (p != null && newH != null && !newH.trim().isEmpty()) {

            ListInterface<String> list = p.getMedicalHistory();

            int pos = list.getPosition(oldH);

            if (pos != -1) {
                boolean success = list.replace(pos, newH);

                if (success) {
                    patientDAO.saveToFile(patientList);
                }

                return success;
            }
        }
        return false;
    }
    // DELETE
    @Override
    public boolean delete(Patient patient) {
        boolean success = patientList.remove(patient);

        if (success) {
            patientDAO.saveToFile(patientList);
        }

        return success;
    }
    

    @Override
    public boolean removePatientAllergy(String patientId, String allergy) {
        Patient p = findById(patientId);

        if (p != null) {
            ListInterface<String> list = p.getAllergies();
            boolean success = list.remove(allergy);

            if (success) {
                patientDAO.saveToFile(patientList);
            }
            return success;
        }
        return false;
    }
    
    @Override
    public boolean removePatientMedicalHistory(String patientId, String history) {
        Patient p = findById(patientId);

        if (p != null) {
            ListInterface<String> list = p.getMedicalHistory();
            boolean success = list.remove(history);

            if (success) {
                patientDAO.saveToFile(patientList);
            }
            return success;
        }
        return false;
    }    
    // SEARCH 
    @Override
    public Patient findById(String id) {
        return patientList.findFirst((Patient p) -> 
                p.getPatientID() != null &&
                p.getPatientID().equalsIgnoreCase(id)
        );
    }
    
    @Override
    public ListInterface<Patient> findByName(String name) {
        return patientList.findAll(p ->
            p.getPatientName() != null &&
            p.getPatientName().toLowerCase()
            .contains(name.toLowerCase())
        );
    }

    @Override
    public ListInterface<Patient> findPatientsWithAllergy() {
        return patientList.findAll((Patient p) ->
            p.getAllergies() != null &&
            !p.getAllergies().isEmpty()
        );
    }
    
    public ListInterface<Patient> findPatientsWithMedicalHistory() {
        return patientList.findAll((Patient p) ->
            p.getMedicalHistory() != null &&
            !p.getMedicalHistory().isEmpty()
        );
    }  
    
    // SORTING 
    @Override
    public ListInterface<Patient> getPatientsSortedByName() {
        ListInterface<Patient> copy = new List<>();
        for (Patient p : patientList) {
            copy.add(p);
        }

        return copy.sort((p1, p2) -> 
                p1.getPatientName().compareToIgnoreCase(p2.getPatientName()));
    }

    @Override
    public ListInterface<Patient> getPatientsSortedByAgeAsc() {
        ListInterface<Patient> copy = new List<>(patientList);
        
        return copy.sort((p1, p2) ->
                Integer.compare(p1.getAge(), p2.getAge()));
    }

    @Override
    public ListInterface<Patient> getPatientsSortedByAgeDesc() {
        ListInterface<Patient> copy = new List<>(patientList);

        return copy.sort((p1, p2) ->
                Integer.compare(p2.getAge(), p1.getAge()));
    }

    // REPORT GENERATION
    @Override
    public String generatePatientReport() {

        ListInterface<Patient> list = findAll();

        if (list.isEmpty()) {
            return "No patient data available.";
        }

        int total = list.getNumberOfEntries();
        int totalAge = 0;
        int minAge = Integer.MAX_VALUE;
        int maxAge = Integer.MIN_VALUE;

        int child = 0,teen = 0, adult = 0, senior = 0;

        for (Patient p : list) {
            int age = p.getAge();

            totalAge += age;

            if (age < minAge) minAge = age;
            if (age > maxAge) maxAge = age;

            // Age Group 
            if (age <= 12) child++;
            else if (age <= 18) teen++;
            else if (age <= 40) adult++;
            else senior++;
        }
        int avgAge = totalAge / total;

        // Safe sorting
        ListInterface<Patient> sortedAsc = getPatientsSortedByAgeAsc();
        ListInterface<Patient> sortedDesc = getPatientsSortedByAgeDesc();        

        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder report = new StringBuilder();

        report.append("============================================================\n");
        report.append("                 CLINIC PATIENT REPORT\n");
        report.append("Generated At: ").append(time).append("\n");
        report.append("============================================================\n\n");

        report.append("[1] SUMMARY\n");
        report.append("----------------------\n");
        report.append("Total Patients : ").append(total).append("\n");
        report.append("Average Age    : ").append(String.format("%d", avgAge)).append("\n");
        report.append("Youngest Age   : ").append(minAge).append("\n");
        report.append("Oldest Age     : ").append(maxAge).append("\n\n");

        report.append("[2] AGE GROUP\n");
        report.append("----------------------\n");       
        report.append("Child (0-12)   : ").append(child).append("\n");
        report.append("Teen (13-18)   : ").append(teen).append("\n");
        report.append("Adult (19-40)  : ").append(adult).append("\n");
        report.append("Senior (41+)   : ").append(senior).append("\n\n");

        report.append("[3] TOP 5 OLDEST\n");
        report.append("----------------------\n"); 
        for (int i = 1; i <= Math.min(5, sortedDesc.getNumberOfEntries()); i++) {
            Patient p = sortedDesc.getEntry(i);
            report.append(p.getPatientID())
                  .append(" - ")
                  .append(p.getPatientName())
                  .append(" (")
                  .append(p.getAge())
                  .append(")\n");
        }
        
        report.append("\n[4] TOP 5 YOUNGEST\n");
        report.append("----------------------\n"); 
        for (int i = 1; i <= Math.min(5, sortedAsc.getNumberOfEntries()); i++) {
            Patient p = sortedAsc.getEntry(i);
            report.append(p.getPatientID())
                  .append(" - ")
                  .append(p.getPatientName())
                  .append(" (")
                  .append(p.getAge())
                  .append(")\n");
        }
        report.append("\n============================================================\n");
        
        return report.toString();
    }

    @Override
    public boolean addPatientAllergy(String patientId, String allergy) {
        Patient p = findById(patientId);
        if (p != null && allergy != null && !allergy.trim().isEmpty()) {
            p.getAllergies().add(allergy);   // ⭐ 操作 List
            patientDAO.saveToFile(patientList);
            return true;
        }
        return false;
    }

    @Override
    public boolean addPatientMedicalHistory(String patientId, String history) {
        Patient p = findById(patientId);
        if (p != null && history != null && !history.trim().isEmpty()) {
            p.getMedicalHistory().add(history);
            patientDAO.saveToFile(patientList);
            return true;
        }
        return false;
    }

    @Override
    public Patient registerPatient(String name, LocalDate birthDate,
                                   ListInterface<String> history,
                                   ListInterface<String> allergy) {

        String id = generatePatientID();
        Patient p = new Patient(id, name, birthDate, history, allergy);
        create(p);
        return p;
    }
}