package Control;

import ADT.ListInterface;
import Entity.Doctor;
import Entity.Appointment;

/**
 * @author Xing Szen
 */
public interface DoctorRepository {

    String generateNextDoctorId();
    
    boolean create(Doctor doctor);

    // Read (searchDoctor, getAllDoctors, getAvailableDoctor)
    ListInterface<Doctor> findAll();

    Doctor findById(String id);

    Doctor findFirstAvailableDoctor(); 
   
    //finders
    ListInterface<Doctor> findBySpecialization(String specialization);

    ListInterface<Doctor> findAllAvailableDoctors();
  
    // Update (updateDoctorDetails)
    boolean update(Doctor doctor);
  
    // Delete (deleteDoctor)
    boolean delete(Doctor doctor);
  
    //Sorting methods
    ListInterface<Doctor> findAllSortedByName();

    ListInterface<Doctor> findAllSortedBySpecialization();
  
    // Logic to check specialization against the collection of doctors
    boolean specializationExists(String specialization);

    // Reporting method
    String generateDoctorReport(ListInterface<Appointment> allApts);
}