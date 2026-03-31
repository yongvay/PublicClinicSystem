package Control;

import ADT.List;
import ADT.SearchCriteria;
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
        for (Doctor d : doctorList) {
            String currentIdStr = d.getDoctorID();
            if (currentIdStr != null && currentIdStr.startsWith("D")) {
                try {
                    int currentIdNum = Integer.parseInt(currentIdStr.substring(1));
                    if (currentIdNum > maxId) {
                        maxId = currentIdNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore bad formatting
                }
            }
        }
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

    
    // SearchCriteria
    
   @Override
    public Doctor findById(final String id) {
        if (id == null) return null;
        
        // Explicitly implementing SearchCriteria
        return doctorList.findFirst(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getDoctorID().equalsIgnoreCase(id);
            }
        });
    }

    @Override
    public Doctor findFirstAvailableDoctor() {
        return doctorList.findFirst(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getStatus();
            }
        });
    }

    @Override
    public ListInterface<Doctor> findBySpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            return new List<>();
        }
        final String searchLower = specialization.toLowerCase();
        
        return doctorList.findAll(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getSpecialization().toLowerCase().contains(searchLower);
            }
        });
    }

    @Override
    public ListInterface<Doctor> findAllAvailableDoctors() {
        return doctorList.findAll(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getStatus();
            }
        });
    }

    @Override
    public boolean specializationExists(final String specialization) {
        Doctor found = doctorList.findFirst(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getSpecialization().equalsIgnoreCase(specialization);
            }
        });
        return found != null;
    }

    // ==========================================
    // REST OF YOUR CODE REMAINS THE SAME
    // ==========================================

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
                    doctorDAO.saveToFile(doctorList); 
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