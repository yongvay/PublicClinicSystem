package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.DoctorDAO; 
import Entity.Doctor;
import java.util.Comparator;

/**
 * @author Xing Szen
 */
public class DoctorRepositoryImpl implements DoctorRepository {

    private ListInterface<Doctor> doctorList;
    private DoctorDAO doctorDAO; 

    public DoctorRepositoryImpl() {
        this.doctorDAO = new DoctorDAO(); 
        this.doctorList = doctorDAO.loadFromFile();

        
        if (this.doctorList == null) {
            this.doctorList = new List<>();
        }
    }
    
    @Override
    public String generateNextDoctorId() {
        int maxId = 0;
        
        // Loop through the custom Iterable List to find the highest ID
        for (Doctor d : doctorList) {
            String currentIdStr = d.getDoctorID();
            
            
            if (currentIdStr != null && currentIdStr.startsWith("D")) {
                try {
                   
                    int currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    
                }
            }
        }
        
        // Add 1 to the max ID found, and format it back to "D" + 3 digits (e.g., D007)
        return String.format("D%03d", maxId + 1);
    }

    @Override
    public boolean create(Doctor doctor) {
        if (doctor != null) {
            if (findById(doctor.getDoctorID()) == null) {
                doctorList.add(doctor);
                doctorDAO.saveToFile(doctorList);
                return true; 
            }
        }
        return false; 
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
            doctorDAO.saveToFile(doctorList); 
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