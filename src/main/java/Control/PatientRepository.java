package Control;

import ADT.ListInterface;
import Entity.Patient;

/**
 * @author Tam Wan Jin
 */
public interface PatientRepository {

    //Auto Generate Patient ID to Create Patient
    String generatePatientID();
    void create(Patient patient);

    // Read
    ListInterface<Patient> findAll();
    
    // Update
    boolean update(Patient patient);

    // Delete
    boolean delete(Patient patient);
    
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
