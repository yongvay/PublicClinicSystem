/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;
import ADT.List;
import ADT.ListInterface;
import DAO.PatientDAO;
import Entity.Patient;
/**
 *
 * @author Tam Wan Jin
 */
public class PatientRepositoryImpl implements PatientRepository {

    private ListInterface<Patient> patientList;
    private PatientDAO patientDAO;

    public PatientRepositoryImpl() {
        patientDAO = new PatientDAO();
        patientList = patientDAO.loadFromFile();
    }
    
    // Auto ID Generation
    @Override
    public String generatePatientID() {
        int max = 0;
        if (patientList.isEmpty())
            return "P001";

        for (Patient p : patientList) {
            int num = Integer.parseInt(p.getPatientID().substring(1));

            if (num > max)
                max = num;
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
    
    // SEARCH
    @Override
    public Patient findById(String id) {
        Patient dummy = new Patient(id);
        int pos = patientList.getPosition(dummy); 

        if (pos != -1) {
            return patientList.getEntry(pos);
        }
        return null;
    }
    
    //Partial Search
    @Override
    public ListInterface<Patient> findByName(String name) {
        ListInterface<Patient> results = new List<>();

        for (Patient p : patientList) {
            if (p.getPatientName().toLowerCase().contains(name.toLowerCase())) {
                results.add(p);
            }
        }
        return results;
    }

    // Search by Allergy
    @Override
    public ListInterface<Patient> findPatientsWithAllergy() {
        ListInterface<Patient> results = new List<>();
        for (Patient p : patientList) {
            if (!p.getAllergies().equalsIgnoreCase("None")) {
                results.add(p);
            }
        }
        return results;
    }
    
    public ListInterface<Patient> getPatientsSortedByName() {
        return patientList.sort((p1, p2) -> p1.getPatientName().compareToIgnoreCase(p2.getPatientName())
        );
    }
    
    public ListInterface<Patient> getPatientsSortedByAgeAsc() {
        return patientList.sort((p1, p2) ->
            Integer.compare(p1.getAge(), p2.getAge())
        );
    }   
    

    public ListInterface<Patient> getPatientsSortedByAgeDesc() {
        return patientList.sort((p1, p2) ->
            Integer.compare(p2.getAge(), p1.getAge())
        );
    }  
       
    // UPDATE
    @Override
    public boolean update(Patient updatedPatient) {
        int pos = patientList.getPosition(updatedPatient); // ✅ ADT

        if (pos != -1) {
            boolean success = patientList.replace(pos, updatedPatient); // ✅ ADT

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
}