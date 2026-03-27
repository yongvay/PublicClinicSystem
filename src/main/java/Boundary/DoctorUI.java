package Boundary;

import Control.DoctorRepository;
import Control.AppointmentRepository;
import Entity.Doctor;
import Entity.Appointment;
import Entity.Medicine;
import ADT.ListInterface;
import ADT.List; 
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Xing Szen
 * Boundary class for the Doctor Subsystem.
 */
public class DoctorUI {

    private DoctorRepository doctorRepo;
    private AppointmentRepository appointmentRepo; // Added for the detailed report
    private Scanner scanner;

    // IMPORTANT: Constructor now requires AppointmentRepository
    public DoctorUI(DoctorRepository doctorRepo, AppointmentRepository appointmentRepo) {
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
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

    private void addDoctor() {
        System.out.println("\n--- Add New Doctor ---");
        System.out.print("Enter Doctor ID: ");
        String id = scanner.nextLine();
        
        if (doctorRepo.findById(id) != null) {
            System.out.println("Error: Doctor ID already exists!");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Specialization: ");
        String spec = scanner.nextLine();
        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();

        Doctor newDoc = new Doctor(id, name, spec, contact, true);
        doctorRepo.create(newDoc);
        System.out.println("Success: Doctor added successfully!");
    }

    private void viewAllDoctors() {
        System.out.println("\n--- All Registered Doctors ---");
        displayList(doctorRepo.findAll());
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
        ListInterface<Doctor> results = doctorRepo.findBySpecialization(spec);
        
        if (results.isEmpty()) {
            System.out.println("No doctors found specializing in: " + spec);
        } else {
            System.out.println("Search Results:");
            displayList(results);
        }
    }

    private void viewAvailableDoctors() {
        System.out.println("\n--- Currently Available Doctors ---");
        ListInterface<Doctor> availableDocs = doctorRepo.findAllAvailableDoctors();
        if (availableDocs.isEmpty()) {
            System.out.println("All doctors are currently occupied or unavailable.");
        } else {
            displayList(availableDocs);
        }
    }

    private void updateDoctor() {
        System.out.print("\nEnter Doctor ID to update: ");
        String id = scanner.nextLine();
        Doctor existing = doctorRepo.findById(id);
        
        if (existing == null) {
            System.out.println("Error: Doctor not found!");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Specialization [" + existing.getSpecialization() + "]: ");
        String spec = scanner.nextLine();
        if (!spec.isEmpty()) existing.setSpecialization(spec);

        System.out.print("New Contact Number [" + existing.getContactNum() + "]: ");
        String contact = scanner.nextLine();
        if (!contact.isEmpty()) existing.setContactNum(contact);
        
        System.out.print("Is Doctor Available? (Y/N) [" + (existing.getStatus() ? "Y" : "N") + "]: ");
        String statusInput = scanner.nextLine().trim();
        if (statusInput.equalsIgnoreCase("Y")) existing.setStatus(true);
        else if (statusInput.equalsIgnoreCase("N")) existing.setStatus(false);

        if (doctorRepo.update(existing)) {
            System.out.println("Success: Doctor details updated successfully!");
        } else {
            System.out.println("Failed to update doctor details.");
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

    private void displayList(ListInterface<Doctor> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Doctor d : list) {
            System.out.println(d.toString());
        }
    }

    // ==========================================
    // REPORT GENERATION (WITH ANALYTICS & EXPORT)
    // ==========================================
    public void generateDoctorReport() {
        ListInterface<Doctor> doctors = doctorRepo.findAll();
        ListInterface<Appointment> appointments = appointmentRepo.getAllAppointments();

        if (doctors.isEmpty()) {
            System.out.println("No doctor data available to generate report.");
            return;
        }

        // We use a StringBuilder so we can print to console AND export to a file easily
        StringBuilder report = new StringBuilder();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        report.append("\n====================================================================================================================================================================\n");
        report.append("                                                         DETAILED DOCTOR PERFORMANCE REPORT                                                                 \n");
        report.append("                                                         Generated At: ").append(time).append("                                                                 \n");
        report.append("====================================================================================================================================================================\n");

        report.append(String.format("| %-9s | %-18s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                "Doctor ID", "Doctor Name", "Total", "Completed", "Waitlisted", "Scheduled", "Patient ID", "Patient Name", "Appt Status", "Treatment (Meds)"));
        report.append("--------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");

        // Variables for Analytics
        ListInterface<String> specializations = new List<>();
        ListInterface<Integer> specApptCounts = new List<>();
        int maxAppts = -1;
        String inDemandSpec = "N/A";

        for (int i = 1; i <= doctors.getNumberOfEntries(); i++) {
            Doctor doc = doctors.getEntry(i);

            int total = 0;
            int completed = 0;
            int waitlisted = 0;
            int scheduled = 0;

            // 1st Pass: Calculate stats
            for (int j = 1; j <= appointments.getNumberOfEntries(); j++) {
                Appointment appt = appointments.getEntry(j);
                if (appt.getDoctor() != null && appt.getDoctor().getDoctorID().equals(doc.getDoctorID())) {
                    total++;
                    String status = appt.getStatus();
                    if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Admitted")) completed++;
                    else if (status.equalsIgnoreCase("Waitlisted")) waitlisted++;
                    else if (status.equalsIgnoreCase("Scheduled")) scheduled++;
                }
            }

            // Analytics: Track Specialization Demand
            String spec = doc.getSpecialization();
            boolean specFound = false;
            for (int s = 1; s <= specializations.getNumberOfEntries(); s++) {
                if (specializations.getEntry(s).equalsIgnoreCase(spec)) {
                    specApptCounts.replace(s, specApptCounts.getEntry(s) + total);
                    specFound = true; break;
                }
            }
            if (!specFound) {
                specializations.add(spec);
                specApptCounts.add(total);
            }

            // SUMMARY ROW: Replaced the "---" with empty spaces ("")
            report.append(String.format("| %-9s | %-18s | %-5d | %-9d | %-10d | %-9d | %-10s | %-18s | %-11s | %-30s |\n",
                    doc.getDoctorID(), doc.getName(), total, completed, waitlisted, scheduled, "", "", "", ""));

            // 2nd Pass: Patient details
            if (total > 0) {
                for (int j = 1; j <= appointments.getNumberOfEntries(); j++) {
                    Appointment appt = appointments.getEntry(j);
                    if (appt.getDoctor() != null && appt.getDoctor().getDoctorID().equals(doc.getDoctorID())) {
                        
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

                        // PATIENT ROW: Only shows patient data, leaving doctor columns blank
                        report.append(String.format("| %-9s | %-18s | %-5s | %-9s | %-10s | %-9s | %-10s | %-18s | %-11s | %-30s |\n",
                                "", "", "", "", "", "", 
                                appt.getPatient().getPatientID(), pName, appt.getStatus(), medsStr));
                    }
                }
            }
            report.append("--------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
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

        // 1. Output to Console
        System.out.println(report.toString());

        // 2. Prompt for External Export
        System.out.print("\nWould you like to export this report to a .txt file? (Y/N): ");
        String exportChoice = scanner.nextLine().trim();
        
        if (exportChoice.equalsIgnoreCase("Y")) {
            try (PrintWriter out = new PrintWriter(new FileWriter("DoctorPerformanceReport.txt"))) {
                out.println(report.toString());
                System.out.println("Success! Report exported to 'DoctorPerformanceReport.txt' in your project directory.");
            } catch (IOException e) {
                System.out.println("Error: Failed to export report. " + e.getMessage());
            }
        }
    }
}