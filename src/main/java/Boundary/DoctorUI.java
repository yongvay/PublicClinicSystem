package Boundary;

import Control.DoctorRepository;
import Control.DoctorRepositoryImpl;
import Entity.Doctor;
import ADT.ListInterface;
import ADT.List; // NEW: Imported for parallel lists in the report
import java.util.Scanner;

/**
 * @author Xing Szen
 * Boundary class for the Doctor Subsystem.
 */
public class DoctorUI {

    private DoctorRepository doctorRepo;
    private Scanner scanner;

    public DoctorUI(DoctorRepository doctorRepo) {
        this.doctorRepo = doctorRepo;
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
        System.out.println("9. View Doctor Report"); // NEW ADDITION
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
            case 9: generateDoctorReport(); break; // NEW ADDITION
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

        // New doctors are available by default
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

    // Iterates through and prints the list
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
    // REPORT GENERATION
    // ==========================================
    public void generateDoctorReport() {
        ListInterface<Doctor> list = doctorRepo.findAll();
        if (list.isEmpty()) {
            System.out.println("No doctor data available.");
            return;
        }

        int totalDoctors = list.getNumberOfEntries();
        int availableCount = 0;
        int occupiedCount = 0;

        // Custom ADT Lists to track specializations and their counts dynamically
        ListInterface<String> specializations = new List<>();
        ListInterface<Integer> specTotalCounts = new List<>();
        ListInterface<Integer> specAvailableCounts = new List<>();

        for (int i = 1; i <= totalDoctors; i++) {
            Doctor d = list.getEntry(i);

            // Track overall availability
            if (d.getStatus()) {
                availableCount++;
            } else {
                occupiedCount++;
            }

            // Track Specialization distribution
            String spec = d.getSpecialization();
            boolean found = false;
            
            for (int j = 1; j <= specializations.getNumberOfEntries(); j++) {
                if (specializations.getEntry(j).equalsIgnoreCase(spec)) {
                    // Update total count for this specialization
                    int currentTotal = specTotalCounts.getEntry(j);
                    specTotalCounts.replace(j, currentTotal + 1);
                    
                    // Update available count for this specialization
                    if (d.getStatus()) {
                        int currentAvail = specAvailableCounts.getEntry(j);
                        specAvailableCounts.replace(j, currentAvail + 1);
                    }
                    found = true;
                    break;
                }
            }
            
            // If it's a new specialization we haven't seen yet
            if (!found) {
                specializations.add(spec);
                specTotalCounts.add(1);
                specAvailableCounts.add(d.getStatus() ? 1 : 0);
            }
        }

        double availabilityRate = (double) availableCount / totalDoctors * 100;
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Print Out Report
        System.out.println("\n======================");
        System.out.println("==== DOCTOR REPORT ===");
        System.out.println("======================");
        System.out.println("Generated At: " + time);
        System.out.println("Total Doctors: " + totalDoctors);

        System.out.println("\n--- Overall Availability ---");
        System.out.println("Available Doctors: " + availableCount);
        System.out.println("Occupied Doctors:  " + occupiedCount);
        System.out.println("Current Availability Rate: " + String.format("%.2f%%", availabilityRate));

        System.out.println("\n--- Distribution by Specialization ---");
        for (int k = 1; k <= specializations.getNumberOfEntries(); k++) {
            String spec = specializations.getEntry(k);
            int tCount = specTotalCounts.getEntry(k);
            int aCount = specAvailableCounts.getEntry(k);
            
            System.out.printf("%-15s : %2d Total ( %2d Available, %2d Occupied )\n", 
                    spec, tCount, aCount, (tCount - aCount));
        }
        System.out.println("======================\n");
    }
}