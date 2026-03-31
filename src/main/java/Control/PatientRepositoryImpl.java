package Control;

import ADT.List;
import ADT.ListInterface;
import ADT.SearchCriteria;
import DAO.PatientDAO;
import Entity.Patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * @author Tam Wan Jin
 */
public class PatientRepositoryImpl implements PatientRepository {

    private ListInterface<Patient> patientList;
    private PatientDAO patientDAO;

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

    // SEARCH BY ID
    @Override
    public Patient findById(String id) {
        return patientList.findFirst(new SearchCriteria<Patient>() {
            @Override
            public boolean isMatch(Patient p) {
                return p.getPatientID() != null &&
                       p.getPatientID().equalsIgnoreCase(id);
            }
        });
    }

    // SEARCH BY NAME 
    @Override
    public ListInterface<Patient> findByName(String name) {
        return patientList.findAll(new SearchCriteria<Patient>() {
            @Override
            public boolean isMatch(Patient p) {
                return p.getPatientName() != null &&
                       p.getPatientName().toLowerCase()
                       .contains(name.toLowerCase());
            }
        });
    }

    // SEARCH: WITH ALLERGY
    @Override
    public ListInterface<Patient> findPatientsWithAllergy() {
        return patientList.findAll((Patient p) -> p.getAllergies() != null &&
                !"None".equalsIgnoreCase(p.getAllergies()));
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
        ListInterface<Patient> copy = new List<>();
        for (Patient p : patientList) {
            copy.add(p);
        }

        return copy.sort((p1, p2) ->
                Integer.compare(p1.getAge(), p2.getAge()));
    }

    @Override
    public ListInterface<Patient> getPatientsSortedByAgeDesc() {
        ListInterface<Patient> copy = new List<>();
        for (Patient p : patientList) {
            copy.add(p);
        }

        return copy.sort((p1, p2) ->
                Integer.compare(p2.getAge(), p1.getAge()));
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

    // DELETE
    @Override
    public boolean delete(Patient patient) {
        boolean success = patientList.remove(patient);

        if (success) {
            patientDAO.saveToFile(patientList);
        }

        return success;
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

        int child = 0;
        int teen = 0;
        int adult = 0;
        int senior = 0;

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
        double avgAge = (double) totalAge / total;

        // Safe sorting
        ListInterface<Patient> sortedAsc = getPatientsSortedByAgeAsc();
        ListInterface<Patient> sortedDesc = getPatientsSortedByAgeDesc();
        ListInterface<Patient> sortedName = getPatientsSortedByName();

        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder report = new StringBuilder();

        report.append("============================================================\n");
        report.append("                 CLINIC PATIENT REPORT\n");
        report.append("Generated At: ").append(time).append("\n");
        report.append("============================================================\n\n");

        report.append("[1] SUMMARY\n");
        report.append("Total Patients : ").append(total).append("\n");
        report.append("Average Age    : ").append(String.format("%.2f", avgAge)).append("\n");
        report.append("Youngest       : ").append(minAge).append("\n");
        report.append("Oldest         : ").append(maxAge).append("\n\n");

        report.append("[2] AGE GROUP\n");
        report.append("Child (0-12)   : ").append(child).append("\n");
        report.append("Teen (13-18)   : ").append(teen).append("\n");
        report.append("Adult (19-40)  : ").append(adult).append("\n");
        report.append("Senior (41+)   : ").append(senior).append("\n\n");

        report.append("[3] TOP 5 OLDEST\n");
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
        for (int i = 1; i <= Math.min(5, sortedAsc.getNumberOfEntries()); i++) {
            Patient p = sortedAsc.getEntry(i);
            report.append(p.getPatientID())
                  .append(" - ")
                  .append(p.getPatientName())
                  .append(" (")
                  .append(p.getAge())
                  .append(")\n");
        }
        
        report.append("\n[5] PATIENT LIST (A-Z)\n");
        for (Patient p : sortedName) {
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
}