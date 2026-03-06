package Control;

import ADT.List;
import ADT.ListInterface;
import Entity.Doctor;
import java.util.Comparator;

/**
 * @author Xing Szen
 * Implementation of the DoctorRepository using the custom List ADT.
 */
public class DoctorRepositoryImpl implements DoctorRepository {

    private ListInterface<Doctor> doctorList;

    public DoctorRepositoryImpl() {
        this.doctorList = new List<>();
    }

    @Override
    public void create(Doctor doctor) {
        if (doctor != null) {
            doctorList.add(doctor);
        }
    }

    @Override
    public ListInterface<Doctor> findAll() {
        return doctorList;
    }

    @Override
    public Doctor findById(String id) {
        if (id == null) return null;
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
        if (specialization == null || specialization.trim().isEmpty()) return results;

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
        if (updatedDoctor == null) return false;

        // Use 1-based index to work with the custom ADT's replace method
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            Doctor current = doctorList.getEntry(i);
            if (current.getDoctorID().equalsIgnoreCase(updatedDoctor.getDoctorID())) {
                return doctorList.replace(i, updatedDoctor);
            }
        }
        return false;
    }

    @Override
    public boolean delete(Doctor doctor) {
        if (doctor == null) return false;
        // Relies on the overridden equals() method in the Doctor entity
        return doctorList.remove(doctor);
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