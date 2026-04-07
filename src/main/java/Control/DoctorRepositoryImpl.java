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
    
    //Auto Generate DoctorId
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
    
   @Override
    public Doctor findById(final String id) {
        if (id == null) return null;
        
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
    public ListInterface<String> getAllUniqueSpecializations() {
        ListInterface<String> uniqueSpecs = new List<>(); 
        
        for (Doctor doc : doctorList) { 
            String spec = doc.getSpecialization();
            boolean exists = false;
            
           
            for (int i = 1; i <= uniqueSpecs.getNumberOfEntries(); i++) { 
                if (uniqueSpecs.getEntry(i).equalsIgnoreCase(spec)) { 
                    exists = true;
                    break;
                }
            }
            
            
            if (!exists && spec != null && !spec.trim().isEmpty()) {
                uniqueSpecs.add(spec); 
            }
        }
        return uniqueSpecs;
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

        report.append(String.format("| %-9s | %-18s | %-15s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                "Doctor ID", "Doctor Name", "Specialization", "Total", "Completed", "Waitlisted", "Scheduled", "Patient ID", "Patient Name", "Appt Status", "Treatment (Meds)"));
        report.append(line);

        ListInterface<String> specializations = new List<>(); 
        ListInterface<Integer> specApptCounts = new List<>(); 
        int maxAppts = -1;
        String inDemandSpec = "N/A";

        for (int i = 1; i <= doctorList.getNumberOfEntries(); i++) { 
            final Doctor doc = doctorList.getEntry(i); 

            int completed = 0;
            int waitlisted = 0;
            int scheduled = 0;

            ListInterface<Appointment> docAppts = appointments.findAll(new SearchCriteria<Appointment>() { 
                @Override
                public boolean isMatch(Appointment a) {
                    return a.getDoctor() != null && a.getDoctor().getDoctorID().equals(doc.getDoctorID());
                }
            });

            int total = docAppts.getNumberOfEntries(); 

            for (Appointment appt : docAppts) { 
                String status = appt.getStatus();
                if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Admitted")) completed++;
                else if (status.equalsIgnoreCase("Waitlisted")) waitlisted++;
                else if (status.equalsIgnoreCase("Scheduled")) scheduled++;
            }

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

            report.append(String.format("| %-9s | %-18s | %-15s | %-5d | %-9d | %-10d | %-9d | %-10s | %-18s | %-11s | %-30s |\n",
                    doc.getDoctorID(), doc.getName(), doc.getSpecialization(), total, completed, waitlisted, scheduled, "", "", "", ""));

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

                    report.append(String.format("| %-9s | %-18s | %-15s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                            "", "", "", "", "", "", "", 
                            appt.getPatient().getPatientID(), pName, appt.getStatus(), medsStr));
                }
            }
            report.append(line);
        }
        
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
    @Override
    public String generateDoctorDashboardHTML(ListInterface<Appointment> appointments) {
        if (doctorList.isEmpty()) {
            return "<html><body><h2>No doctor data available.</h2></body></html>";
        }

        ListInterface<String> specs = getAllUniqueSpecializations();
        ListInterface<Integer> docCounts = new List<>();
        ListInterface<Integer> apptCounts = new List<>();
        
        int maxAppts = 0;
        int totalSystemDocs = doctorList.getNumberOfEntries();

        for (int i = 1; i <= specs.getNumberOfEntries(); i++) {
            final String currentSpec = specs.getEntry(i);
            
            ListInterface<Doctor> docsInSpec = doctorList.findAll(new SearchCriteria<Doctor>() {
                @Override
                public boolean isMatch(Doctor d) {
                    return d.getSpecialization().equalsIgnoreCase(currentSpec);
                }
            });
            int docCount = docsInSpec.getNumberOfEntries();
            docCounts.add(docCount);

            int apptCount = 0;
            for (int j = 1; j <= docsInSpec.getNumberOfEntries(); j++) {
                final Doctor doc = docsInSpec.getEntry(j);
                
                ListInterface<Appointment> docAppts = appointments.findAll(new SearchCriteria<Appointment>() {
                    @Override
                    public boolean isMatch(Appointment a) {
                        return a.getDoctor() != null && a.getDoctor().getDoctorID().equals(doc.getDoctorID());
                    }
                });
                apptCount += docAppts.getNumberOfEntries();
            }
            apptCounts.add(apptCount);
            
            if (apptCount > maxAppts) {
                maxAppts = apptCount;
            }
        }

        StringBuilder conicGradient = new StringBuilder();
        StringBuilder legendHtml = new StringBuilder();
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#E7E9ED"};
        double currentPercentage = 0;

        for(int i = 1; i <= specs.getNumberOfEntries(); i++) {
            int count = docCounts.getEntry(i);
            if(count == 0) continue;
            
            double slicePercentage = ((double) count / totalSystemDocs) * 100;
            String color = colors[(i - 1) % colors.length];

            conicGradient.append(color).append(" ").append(currentPercentage).append("% ").append(currentPercentage + slicePercentage).append("%, ");
            
            legendHtml.append("<div class='legend-item'>")
                      .append("<div class='legend-color' style='background-color:").append(color).append(";'></div>")
                      .append("<span>").append(specs.getEntry(i)).append(" (").append(count).append(" doctors)</span></div>\n");

            currentPercentage += slicePercentage;
        }

        if(conicGradient.length() > 0) conicGradient.setLength(conicGradient.length() - 2);

        StringBuilder html = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Specialization Analytics Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6; color: #333; margin: 40px; }\n");
        html.append(".container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); max-width: 900px; margin: auto; }\n");
        html.append("h1, h3 { text-align: center; color: #2c3e50; }\n");
        
        html.append(".dashboard-grid { display: flex; justify-content: space-between; flex-wrap: wrap; margin-top: 30px; }\n");
        html.append(".chart-section { width: 45%; padding: 20px; background: #fafafa; border-radius: 8px; border: 1px solid #eee; }\n");

        html.append(".pie-chart { width: 250px; height: 250px; border-radius: 50%; margin: 20px auto; background: conic-gradient(").append(conicGradient.toString()).append("); }\n");
        html.append(".legend-item { display: flex; align-items: center; margin-bottom: 8px; font-size: 14px; }\n");
        html.append(".legend-color { width: 16px; height: 16px; border-radius: 4px; margin-right: 10px; }\n");

        html.append(".bar-row { display: flex; align-items: center; margin-bottom: 15px; }\n");
        html.append(".bar-label { width: 120px; font-size: 14px; font-weight: bold; text-align: right; margin-right: 15px; }\n");
        html.append(".bar-container { flex-grow: 1; background-color: #e0e0e0; border-radius: 5px; height: 25px; overflow: hidden; }\n");
        html.append(".bar-fill { height: 100%; background-color: #3498db; display: flex; align-items: center; padding-left: 10px; color: white; font-weight: bold; font-size: 12px; }\n");
        
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"container\">\n");
        html.append("<h1>Clinic Specialization Report</h1>\n");
        html.append("<p style=\"text-align:center;\">Report Generated At: <strong>").append(time).append("</strong></p>\n");
        html.append("<hr style=\"border: 1px solid #eee; margin: 20px 0;\">\n");

        html.append("<div class=\"dashboard-grid\">\n");

        html.append("  <div class=\"chart-section\">\n");
        html.append("    <h3>Distribution of Doctors</h3>\n");
        html.append("    <div class=\"pie-chart\"></div>\n");
        html.append("    <div style=\"margin-top: 20px;\">").append(legendHtml.toString()).append("</div>\n");
        html.append("  </div>\n");

        html.append("  <div class=\"chart-section\">\n");
        html.append("    <h3>Total Appointments Handled</h3>\n");
        html.append("    <div style=\"margin-top: 30px;\">\n");

        for (int i = 1; i <= specs.getNumberOfEntries(); i++) {
            String spec = specs.getEntry(i);
            int count = apptCounts.getEntry(i);
            
            int percentage = (maxAppts == 0) ? 0 : (int) Math.round(((double) count / maxAppts) * 100);
            if (percentage < 5 && count > 0) percentage = 5; 

            html.append("      <div class=\"bar-row\">\n");
            html.append("        <div class=\"bar-label\">").append(spec).append("</div>\n");
            html.append("        <div class=\"bar-container\">\n");
            html.append("          <div class=\"bar-fill\" style=\"width: ").append(percentage).append("%;\">").append(count).append("</div>\n");
            html.append("        </div>\n");
            html.append("      </div>\n");
        }

        html.append("    </div>\n");
        html.append("  </div>\n");

        html.append("</div>\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");

        return html.toString();
    }
}