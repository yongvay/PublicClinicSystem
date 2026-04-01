package Boundary;

import Control.DoctorRepository;
import Control.AppointmentRepository;
import Entity.Doctor;
import Entity.Appointment;
import ADT.ListInterface;
import ADT.SearchCriteria;
import Utility.Utilities; 
import java.util.Scanner;   

/**
 * @author Xing Szen
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

    public void start() {
        int choice = -1;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
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
        
        // 1. Auto-generate the ID
        String id = doctorRepo.generateNextDoctorId();
        System.out.println("Auto-generated Doctor ID: " + id);

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

    // clean up logic
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

    private void displayList(ListInterface<Doctor> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("The list is empty.");
            return;
        }
        for (Doctor d : list) {
            System.out.println(d.toString());
        }
    }

   
    // REPORT GENERATION 
 
    public void generateDoctorReport() {
        ListInterface<Appointment> appointments = appointmentRepo.getAllAppointments();

        String reportText = doctorRepo.generateDoctorReport(appointments);
        System.out.println(reportText);

        if (!reportText.equals("No doctor data available to generate report.\n")) {
            System.out.print("\nWould you like to export this report to a .txt file? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();
            
            if (exportChoice.equalsIgnoreCase("Y")) {
               
                Utilities.exportReportToFile(reportText, "DoctorPerformanceReport.txt");
            }
        }
    }
}