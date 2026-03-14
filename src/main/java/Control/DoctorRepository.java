package Control;

import ADT.ListInterface;
import Entity.Doctor;

/**
 * @author Xing Szen
 */
public interface DoctorRepository {

    // Create 
    void create(Doctor doctor);

    // Read (searchDoctor, getAllDoctors, getAvailableDoctor)
    ListInterface<Doctor> findAll();

    Doctor findById(String id);

    Doctor findFirstAvailableDoctor(); // fetch one free doctor for the queue

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
}
