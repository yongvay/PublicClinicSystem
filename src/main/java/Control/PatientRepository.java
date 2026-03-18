/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;
import ADT.ListInterface;
import Entity.Patient;

/**
 *
 * @author Tam Wan Jin
 */
public interface PatientRepository {

    //Auto Generate Patient ID
    String generatePatientID();
    
    // Create
    void create(Patient patient);

    // Read
    ListInterface<Patient> findAll();
    Patient findById(String id);
    ListInterface<Patient> findByName(String name);
    ListInterface<Patient> findPatientsWithAllergy();
    
    // Update
    boolean update(Patient patient);

    // Delete
    boolean delete(Patient patient);
}
