package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.DoctorDAO; // Import your new DAO
import Entity.Doctor;
import java.util.Comparator;

/**
 * @author Xing Szen
 */
public class DoctorRepositoryImpl implements DoctorRepository {

    private ListInterface<Doctor> doctorList;
    private DoctorDAO doctorDAO; // Declare the DAO

    public DoctorRepositoryImpl() {
        this.doctorDAO = new DoctorDAO(); // Initialize the DAO
        // Load the data from the text file instead of starting empty!
        this.doctorList = doctorDAO.loadFromFile();

        // Failsafe: If the file was completely empty, initialize an empty list
        if (this.doctorList == null) {
            this.doctorList = new List<>();
        }
    }

    @Override
    public void create(Doctor doctor) {
        if (doctor != null) {
            // Failsafe: Check if the ID already exists before adding
            if (findById(doctor.getDoctorID()) == null) {
                doctorList.add(doctor);
                doctorDAO.saveToFile(doctorList);
            } else {
                // Silent fail for backend, UI handles the actual error message
                System.err.println("Error : The enterered ID is a;ready exist. " + doctor.getDoctorID());
            }
        }
    }

    @Override
    public ListInterface<Doctor> findAll() {
        return doctorList;
    }

    @Override
    public Doctor findById(String id) {
        if (id == null) {
            return null;
        }
        for (Doctor d : doctorList) {
            if (d.getDoctorID().equalsIgnoreCase(id)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public Doctor findFirstAvailableDoctor() {
        for (Doctor d : doctorList) {
            if (d.getStatus()) {
                return d;
            }
        }
        return null;
    }

    @Override
    public ListInterface<Doctor> findBySpecialization(String specialization) {
        ListInterface<Doctor> results = new List<>();
        if (specialization == null || specialization.trim().isEmpty()) {
            return results;
        }

        String searchLower = specialization.toLowerCase();
        for (Doctor d : doctorList) {
            if (d.getSpecialization().toLowerCase().contains(searchLower)) {
                results.add(d);
            }
        }
        return results;
    }

    @Override
    public ListInterface<Doctor> findAllAvailableDoctors() {
        ListInterface<Doctor> results = new List<>();
        for (Doctor d : doctorList) {
            if (d.getStatus()) {
                results.add(d);
            }
        }
        return results;
    }

    @Override
    public boolean update(Doctor updatedDoctor) {
        if (updatedDoctor == null) {
            return false;
        }

        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor current = doctorList.getEntry(i);
            if (current.getDoctorID().equalsIgnoreCase(updatedDoctor.getDoctorID())) {
                boolean success = doctorList.replace(i, updatedDoctor);
                if (success) {
                    doctorDAO.saveToFile(doctorList); // SAVE TO FILE AFTER UPDATING
                }
                return success;
            }
        }
        return false;
    }

    @Override
    public boolean delete(Doctor doctor) {
        if (doctor == null) {
            return false;
        }

        boolean success = doctorList.remove(doctor);
        if (success) {
            doctorDAO.saveToFile(doctorList); // SAVE TO FILE AFTER DELETING
        }
        return success;
    }

    @Override
    public boolean specializationExists(String specialization) {
        // Iterate through our custom ADT
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            if (doctorList.getEntry(i).getSpecialization().equalsIgnoreCase(specialization)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ListInterface<Doctor> findAllSortedByName() {
        return doctorList.sort(new Comparator<Doctor>() {
            @Override
            public int compare(Doctor d1, Doctor d2) {
                return d1.getName().compareToIgnoreCase(d2.getName());
            }
        });
    }

    @Override
    public ListInterface<Doctor> findAllSortedBySpecialization() {
        return doctorList.sort(new Comparator<Doctor>() {
            @Override
            public int compare(Doctor d1, Doctor d2) {
                return d1.getSpecialization().compareToIgnoreCase(d2.getSpecialization());
            }
        });
    }
}
