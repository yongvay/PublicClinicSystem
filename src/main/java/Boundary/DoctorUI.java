package Boundary;

import Control.DoctorRepository;
import Control.AppointmentRepository;
import Entity.Doctor;
import Entity.Appointment;
import Entity.Medicine;
import ADT.List;
import ADT.ListInterface;
import ADT.SearchCriteria;
import Utility.Utilities; 
import java.util.Scanner;   
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Xing Szen
 * Boundary class for the Doctor Subsystem.
 * Handles all user interactions (input/output).
 */
public class DoctorUI {

    private DoctorRepository doctorRepo;
    private AppointmentRepository appointmentRepo; 
    private Scanner scanner;

    public DoctorUI(DoctorRepository doctorRepo, AppointmentRepository appointmentRepo) {
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.scanner = new Scanner(System.in);
    }

    // ==========================================
    // MAIN START METHOD
    // ==========================================
    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            // Basic input validation to prevent crashes
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the leftover newline character
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the bad input
            }
        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n==========================================");
        System.out.println("       CLINIC SUBSYSTEM: DOCTOR MENU      ");
        System.out.println("==========================================");
        System.out.println("1. Add New Doctor");
        System.out.println("2. View All Doctors");
        System.out.println("3. Search Doctor by ID");
        System.out.println("4. Search Doctor by Specialization");
        System.out.println("5. View All Available Doctors");
        System.out.println("6. Update Doctor Details");
        System.out.println("7. Remove Doctor");
        System.out.println("8. View Sorted Doctors (By Name/Specialization)");
        System.out.println("9. View Doctor Report");
        System.out.println("0. Exit to Main Menu");
        System.out.println("==========================================");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: addDoctor(); break;
            case 2: viewAllDoctors(); break;
            case 3: searchById(); break;
            case 4: searchBySpecialization(); break;
            case 5: viewAvailableDoctors(); break;
            case 6: updateDoctor(); break;
            case 7: deleteDoctor(); break;
            case 8: viewSortedDoctors(); break;
            case 9: generateDoctorReport(); break; 
            case 0: System.out.println("Exiting Doctor Subsystem..."); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private void addDoctor() {
        System.out.println("\n--- Add New Doctor ---");
        
        // 1. Auto-generate the ID
        String id = doctorRepo.generateNextDoctorId();
        System.out.println("Auto-generated Doctor ID: " + id);

        // 2. Continue asking for details
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Specialization: ");
        String spec = scanner.nextLine();
        
        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();

        // New doctors are available by default
        Doctor newDoc = new Doctor(id, name, spec, contact, true);
        doctorRepo.create(newDoc);
        System.out.println("Success: Doctor added successfully!");
    }

    private void viewAllDoctors() {
        System.out.println("\n--- All Registered Doctors ---");
        ListInterface<Doctor> list = doctorRepo.findAll();
        displayList(list);
    }

    private void searchById() {
        System.out.print("\nEnter Doctor ID to search: ");
        String id = scanner.nextLine();
        Doctor found = doctorRepo.findById(id);
        
        if (found != null) {
            System.out.println("Doctor Found: \n" + found.toString());
        } else {
            System.out.println("Doctor not found with ID: " + id);
        }
    }

    private void searchBySpecialization() {
        System.out.print("\nEnter Specialization to search (e.g., Cardiology): ");
        String spec = scanner.nextLine();
        final String searchLower = spec.toLowerCase();

        
        ListInterface<Doctor> allDocs = doctorRepo.findAll();
        
        ListInterface<Doctor> results = allDocs.findAll(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getSpecialization().toLowerCase().contains(searchLower);
            }
        });
        
        if (results.isEmpty()) {
            System.out.println("No doctors found specializing in: " + spec);
        } else {
            System.out.println("Search Results:");
            displayList(results);
        }
    }

    private void viewAvailableDoctors() {
        System.out.println("\n--- Currently Available Doctors ---");
        
        ListInterface<Doctor> allDocs = doctorRepo.findAll();
        
        ListInterface<Doctor> availableDocs = allDocs.findAll(new SearchCriteria<Doctor>() {
            @Override
            public boolean isMatch(Doctor doctor) {
                return doctor.getStatus() == true;
            }
        });

        if (availableDocs.isEmpty()) {
            System.out.println("All doctors are currently occupied or unavailable.");
        } else {
            displayList(availableDocs);
        }
    }

    private void updateDoctor() {
        Utilities.printHeader("Update Doctor Details");
        String id = Utilities.getString("Enter Doctor ID to update: ");
        Doctor existing = doctorRepo.findById(id);
        
        if (existing == null) {
            System.out.println("Error: Doctor not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        // Using functional interface helper
        updateIfNotEmpty(Utilities.getString("New Name [" + existing.getName() + "]: "), existing::setName);
        updateIfNotEmpty(Utilities.getString("New Specialization [" + existing.getSpecialization() + "]: "), existing::setSpecialization);
        updateIfNotEmpty(Utilities.getString("New Contact Number [" + existing.getContactNum() + "]: "), existing::setContactNum);
        
        String statusInput = Utilities.getString("Is Doctor Available? (Y/N) [" + (existing.getStatus() ? "Y" : "N") + "]: ").trim();
        if (statusInput.equalsIgnoreCase("Y")) existing.setStatus(true);
        else if (statusInput.equalsIgnoreCase("N")) existing.setStatus(false);

        if (doctorRepo.update(existing)) {
            System.out.println("Success: Doctor details updated successfully!");
        } else {
            System.out.println("Failed to update doctor details.");
        }
    }

    // A functional interface helper to clean up the "if not empty, set" logic
    private void updateIfNotEmpty(String input, java.util.function.Consumer<String> setter) {
        if (!input.isEmpty()) {
            setter.accept(input);
        }
    }

    private void deleteDoctor() {
        System.out.print("\nEnter Doctor ID to delete: ");
        String id = scanner.nextLine();
        Doctor target = doctorRepo.findById(id);
        
        if (target != null) {
            System.out.print("Are you sure you want to delete " + target.getName() + "? (Y/N): ");
            String confirm = scanner.nextLine();
            if (confirm.equalsIgnoreCase("Y")) {
                if (doctorRepo.delete(target)) {
                    System.out.println("Success: Doctor deleted.");
                } else {
                    System.out.println("Error: Could not delete doctor.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("Error: Doctor not found.");
        }
    }

    private void viewSortedDoctors() {
        System.out.println("\n--- Sort Doctors ---");
        System.out.println("1. Sort by Name (A-Z)");
        System.out.println("2. Sort by Specialization (A-Z)");
        System.out.print("Enter choice: ");
        
        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            ListInterface<Doctor> sortedList = null;
            if (choice == 1) {
                sortedList = doctorRepo.findAllSortedByName();
            } else if (choice == 2) {
                sortedList = doctorRepo.findAllSortedBySpecialization();
            } else {
                System.out.println("Invalid choice.");
                return;
            }
            displayList(sortedList);
        } else {
            System.out.println("Invalid input.");
            scanner.nextLine();
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================
    private void displayList(ListInterface<Doctor> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("The list is empty.");
            return;
        }
        // Using the enhanced for-loop because your List implements Iterable!
        for (Doctor d : list) {
            System.out.println(d.toString());
        }
    }

    // ==========================================
    // REPORT GENERATION (FULLY ADT OPTIMIZED)
    // ==========================================
    public void generateDoctorReport() {
        ListInterface<Doctor> doctors = doctorRepo.findAll();
        ListInterface<Appointment> appointments = appointmentRepo.getAllAppointments();

        if (doctors.isEmpty()) {
            System.out.println("No doctor data available to generate report.");
            return;
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
        for (int i = 1; i <= doctors.getNumberOfEntries(); i++) {
            final Doctor doc = doctors.getEntry(i); 

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

        System.out.println(report.toString());

        System.out.print("\nWould you like to export this report to a .txt file? (Y/N): ");
        String exportChoice = scanner.nextLine().trim();
        
        if (exportChoice.equalsIgnoreCase("Y")) {
            // CALL THE SHARED UTILITY METHOD HERE
            Utilities.exportReportToFile(report.toString(), "DoctorPerformanceReport.txt");
        }
    }
    
}