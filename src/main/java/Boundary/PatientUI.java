package Boundary;

import ADT.ListInterface;
import Control.PatientRepository;
import Entity.Patient;
import Utility.Utilities;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
/**
 * @author Tam Wan Jin
 */
public class PatientUI {

    private final PatientRepository patientRepo;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PatientUI(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
        this.scanner = new Scanner(System.in);
    }

    // INPUT HELPERS
    private String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private String inputWithMaxLength(String prompt, int maxLength, boolean allowEmpty) {
       while (true) {
           System.out.print(prompt);
           String input = scanner.nextLine().trim();
           if (!allowEmpty && input.isEmpty()) {
               System.out.println("Input cannot be empty. Please enter again.");
           } else if (input.length() > maxLength) {
               System.out.printf("Input too long! Maximum %d characters allowed.\n", maxLength);
           } else {
               return input;
           }
       }
   }   

    private LocalDate getValidBirthDate(String input) {
        try {
            LocalDate date = LocalDate.parse(input, formatter);

            if (date.isAfter(LocalDate.now())) {
                System.out.println("Birth date cannot be in the future.");
                return null;
            }

            return date;

        } catch (DateTimeParseException e) {
            System.out.println("Invalid format (dd/MM/yyyy).");
            return null;
        }
    }

    private LocalDate addBirthDate() {
        while (true) {
            String input = inputString("Birth Date (dd/MM/yyyy): ");
            LocalDate date = getValidBirthDate(input);

            if (date != null) return date;
        }
    }

    private LocalDate updateBirthDate(LocalDate currentDate) {
        while (true) {
            String input = inputString(
                    "New Birth Date (dd/MM/yyyy): ");

            if (input.isEmpty()) return currentDate;

            LocalDate date = getValidBirthDate(input);
            if (date != null) return date;
        }
    }

    // MAIN MENU
    public void start() {

        int choice;

        do {
            displayMenu();
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addPatient();
                case 2 -> viewAllPatients();
                case 3 -> updatePatient();
                case 4 -> deletePatient();
                case 5 -> searchById();
                case 6 -> searchByName();
                case 7 -> searchPatientsWithAllergy();
                case 8 -> printAllPatientsSortedByName();
                case 9 -> generateReport();               
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }

        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n========== PATIENT MENU ==========");
        System.out.println("1 Add Patient");
        System.out.println("2 View All Patients");
        System.out.println("3 Update Patient");
        System.out.println("4 Delete Patient");
        System.out.println("5 Search by ID");
        System.out.println("6 Search by Name");
        System.out.println("7 Search Patient with Allergy");
        System.out.println("8 View Patient List (A-Z)");
        System.out.println("9 Generate Patient Report");        
        System.out.println("0 Exit");
        System.out.println("=================================");
    }

    // CREATE
    public void addPatient() {
        System.out.println("\n--- Add New Patient ---");
        String id = patientRepo.generatePatientID();

        // Input with max length validation
        String name = inputWithMaxLength("Patient Name: ", 20, false);
        LocalDate birthDate = addBirthDate();
        
        // History input
        String history;
        while (true) {
            System.out.print("Does the patient have medical history? (Yes/No): ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("Yes")) {
                history = inputWithMaxLength("Enter History: ", 30, false); // Max 15 for table formatting
                break;
            } else if (choice.equalsIgnoreCase("No")) {
                history = "None";
                break;
            } else {
                System.out.println("Invalid input. Please enter Yes or No.");
            }
        }
        
        // Allergies input
        String allergies;
        while (true) {
            System.out.print("Does the patient have allergies? (Yes/No): ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("Yes")) {
                allergies = inputWithMaxLength("Enter allergy: ", 15, false); // Max 15 for table formatting
                break;
            } else if (choice.equalsIgnoreCase("No")) {
                allergies = "None";
                break;
            } else {
                System.out.println("Invalid input. Please enter Yes or No.");
            }
        }

        Patient p = new Patient(id, name, birthDate, history, allergies);
        patientRepo.create(p);
        System.out.println("Patient added successfully.");
    }

    // READ
    private void viewAllPatients() {
        System.out.println("\n--- List of All Patient ---");
        displayList(patientRepo.findAll());
    }

    // SEARCH
    private void searchById() {
        System.out.println("\n--- Search Patient by ID ---");
        String id = inputString("Enter ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null)
            System.out.println("Patient not found.");
        else
            System.out.println(p);
    }

    private void searchByName() {
        System.out.println("\n--- Search Patient by Name---");
        String name = inputString("Enter name: "); 
        displayList(patientRepo.findByName(name));
    }

    private void searchPatientsWithAllergy() {
        System.out.println("\n--- CAUTION: Patient with Allergy ---");
        displayList(patientRepo.findPatientsWithAllergy());
    }

    // UPDATE
    private void updatePatient() {
        System.out.println("\n--- Update Patient ---");
        System.out.print("<Enter Patient ID to update> ");
        String id = scanner.nextLine().trim();
        Patient existing = patientRepo.findById(id);

        if (existing == null) {
            System.out.println("\nPatient not found. Failed to update.");
            return;
        }

        System.out.println("\nCurrent Details: " + existing.toString());
        System.out.println("Enter new details <press Enter to keep current value>\n");

        // Update Name
        String name = inputWithMaxLength("New Name: ", 20, true);
        if (!name.isEmpty()) existing.setPatientName(name);

        // Update BirthDate
        existing.setBirthDate(updateBirthDate(existing.getBirthDate()));

        // Update Medical History
        String history = inputWithMaxLength("New Medical History: ", 30, true
        );
        if (!history.isEmpty()) existing.setMedicalHistory(history);

        // Update Allergies
        String allergies = inputWithMaxLength("New Allergies: ", 15, true);
        if (!allergies.isEmpty()) existing.setAllergies(allergies);

        patientRepo.update(existing);
        System.out.println("Patient updated successfully.");
    }

    // DELETE
    private void deletePatient() {
        System.out.println("\n--- Delete Patient ---");
        String id = inputString("Enter ID: ");
        Patient existing = patientRepo.findById(id);

        if (existing == null) {
            System.out.println("\nPatient not found. Failed to update.");
            return;
        }

        System.out.println("\nCurrent Details: " + existing.toString());
        
        System.out.print("\nEnter 'Y' to confirm deletion? (Y/N): ");
        String exportChoice = scanner.nextLine().trim();

        if (exportChoice.equalsIgnoreCase("Y")) {
            patientRepo.delete(existing);
            System.out.println("Deleted successfully.");            
        }
    }

    // DISPLAY
    private void displayList(ListInterface<Patient> list) {

        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        for (Patient p : list) {
            System.out.println(p);
        }
    }
    
    // REPORT
    private void generateReport() {
        String report = patientRepo.generatePatientReport();
        System.out.println(report);

        if (!report.equals("No medicine data available to generate report.\n")) {
            System.out.print("\nWould you like to export this report to a .txt file? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                Utilities.exportReportToFile(report, "PatientReport.txt");
            }
        }
    } 
    
    public void printAllPatientsSortedByName() {

        ListInterface<Patient> sortedList = patientRepo.getPatientsSortedByName();

        if (sortedList.isEmpty()) {
            System.out.println("No patient records found.");
            return;
        }

        System.out.println("\n===== PATIENT LIST (A-Z) =====");
        for (Patient p : sortedList) {
            System.out.println(p); 
        }
    }   
}