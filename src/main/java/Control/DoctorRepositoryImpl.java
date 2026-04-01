package Control;

import ADT.List;
import ADT.SearchCriteria;
import ADT.ListInterface;
import DAO.DoctorDAO; 
import Entity.Doctor;
import Entity.Appointment;
import Entity.Medicine;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        int position = doctorList.getPosition(updatedDoctor);

        if (position != -1) {
            boolean success = doctorList.replace(position, updatedDoctor);
            if (success) {
                doctorDAO.saveToFile(doctorList); 
            }
            return success;
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

    @Override
    public String generateDoctorReport(ListInterface<Appointment> appointments) {
        if (doctorList.isEmpty()) {
            return "No doctor data available to generate report.\n";
        }

        StringBuilder report = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        String separator = "======================================================================================================================================================================================\n";
        String line =      "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";

        report.append("\n").append(separator);
        report.append("                                                                         DETAILED DOCTOR PERFORMANCE REPORT                                                                           \n");
        report.append("                                                                         Generated At: ").append(time).append("                                                                           \n");
        report.append(separator);

        // ADDED SPECIALIZATION COLUMN HERE
        report.append(String.format("| %-9s | %-18s | %-15s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                "Doctor ID", "Doctor Name", "Specialization", "Total", "Completed", "Waitlisted", "Scheduled", "Patient ID", "Patient Name", "Appt Status", "Treatment (Meds)"));
        report.append(line);

        ListInterface<String> specializations = new List<>();
        ListInterface<Integer> specApptCounts = new List<>();   
        int maxAppts = -1;
        String inDemandSpec = "N/A";

        // Generate report per doctor
        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) {
            final Doctor doc = doctorList.getEntry(i); 

            int completed = 0;
            int waitlisted = 0;
            int scheduled = 0;

            // ADT OPTIMIZATION 1: Instantly filter appointments for this specific doctor using SearchCriteria!
            ListInterface<Appointment> docAppts = appointments.findAll(new SearchCriteria<Appointment>() {
                @Override
                public boolean isMatch(Appointment a) {
                    return a.getDoctor() != null && a.getDoctor().getDoctorID().equals(doc.getDoctorID());
                }
            });

            int total = docAppts.getNumberOfEntries();

            // Loop through this doctor's specific appointments
            for (Appointment appt : docAppts) {
                String status = appt.getStatus();
                if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Admitted")) completed++;
                else if (status.equalsIgnoreCase("Waitlisted")) waitlisted++;
                else if (status.equalsIgnoreCase("Scheduled")) scheduled++;
            }

            // Safe, case-insensitive manual tracking for parallel lists
            String spec = doc.getSpecialization();
            int pos = -1;
            for (int s = 1; s <= specializations.getNumberOfEntries(); s++) {
                if (specializations.getEntry(s).equalsIgnoreCase(spec)) {
                    pos = s;
                    break;
                }
            }
            
            if (pos != -1) {
                specApptCounts.replace(pos, specApptCounts.getEntry(pos) + total);
            } else {
                specializations.add(spec);
                specApptCounts.add(total);
            }

            // SUMMARY ROW (ADDED SPECIALIZATION VARIABLE)
            report.append(String.format("| %-9s | %-18s | %-15s | %-5d | %-9d | %-10d | %-9d | %-10s | %-18s | %-11s | %-30s |\n",
                    doc.getDoctorID(), doc.getName(), doc.getSpecialization(), total, completed, waitlisted, scheduled, "", "", "", ""));

            // PATIENT DETAILS
            if (total > 0) {
                for (Appointment appt : docAppts) {
                    String medsStr = "None";
                    ListInterface<Medicine> meds = appt.getPrescribedMedicines();
                    if (meds != null && !meds.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (int k = 1; k <= meds.getNumberOfEntries(); k++) {
                            sb.append(meds.getEntry(k).getName()).append(", ");
                        }
                        medsStr = sb.substring(0, sb.length() - 2); 
                    }

                    String pName = appt.getPatient().getPatientName();
                    if (pName.length() > 18) pName = pName.substring(0, 15) + "...";
                    if (medsStr.length() > 30) medsStr = medsStr.substring(0, 27) + "...";

                    // SHIFTED OVER TO ACCOUNT FOR NEW SPECIALIZATION COLUMN
                    report.append(String.format("| %-9s | %-18s | %-15s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                            "", "", "", "", "", "", "", 
                            appt.getPatient().getPatientID(), pName, appt.getStatus(), medsStr));
                }
            }
            report.append(line);
        }
        
        // Calculate Most In-Demand Specialization
        for (int s = 1; s <= specializations.getNumberOfEntries(); s++) {
            if (specApptCounts.getEntry(s) > maxAppts) {
                maxAppts = specApptCounts.getEntry(s);
                inDemandSpec = specializations.getEntry(s);
            }
        }

        report.append("\n[ADVANCED ANALYTICS] Resource Utilization\n");
        report.append("Most In-Demand Specialization: ").append(inDemandSpec).append(" (").append(maxAppts).append(" total appointments)\n");
        report.append("End of Report.\n");

        return report.toString();
    }
}