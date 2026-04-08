package Control;

import ADT.ListInterface;
import Entity.Patient;
import java.time.LocalDate;

/**
 * @author Tam Wan Jin
 */
public interface PatientRepository {

    //Auto Generate Patient ID to Create Patient
    String generatePatientID();
    void create(Patient patient);
    boolean addPatientMedicalHistory(String id, String newH);
    boolean addPatientAllergy(String id, String newA);
    Patient registerPatient(String name, LocalDate birthDate, ListInterface<String> history, ListInterface<String> allergy); 
   
    // Read
    ListInterface<Patient> findAll();
    
    // Update
    boolean update(Patient patient);
    boolean updatePatientAllergy(String id, String oldA, String newA);
    boolean updatePatientMedicalHistory(String id, String oldH, String newH);
    
    // Delete
    boolean delete(Patient patient);
    boolean removePatientAllergy(String id, String removeA);
    boolean removePatientMedicalHistory(String id, String removeH);
    
    // Search
    Patient findById(String id);
    ListInterface<Patient> findByName(String name);
    ListInterface<Patient> findPatientsWithAllergy();
    
    // Sorting
    ListInterface<Patient> getPatientsSortedByName();
    ListInterface<Patient> getPatientsSortedByAgeAsc();
    ListInterface<Patient> getPatientsSortedByAgeDesc();
    
    // Generate Report
    String generatePatientReport();
}