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

    // SAFE INPUT (avoid crash)
    private int inputInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty.");
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String inputWithMaxLength(String prompt, int maxLength, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!allowEmpty && input.isEmpty()) {
                System.out.println("Cannot be empty.");
            } else if (input.length() > maxLength) {
                System.out.println("Too long (max " + maxLength + ")");
            } else {
                return input;
            }
        }
    }

    // DATE VALIDATION
    private LocalDate getValidBirthDate(String input) {
        try {
            LocalDate date = LocalDate.parse(input, formatter);
            if (date.isAfter(LocalDate.now())) {
                System.out.println("Cannot be future date.");
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            System.out.println("Format must be dd/MM/yyyy");
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
            String input = inputString("New Birth Date (Enter to skip): ");
            if (input.isEmpty()) return currentDate;
            LocalDate date = getValidBirthDate(input);
            if (date != null) return date;
        }
    }

    // TABLE FORMAT 
    private String limit(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
    
    // DISPLAY
    private void displayList(ListInterface<Patient> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("=================================================================================================");
        System.out.printf("%-6s | %-15s | %-4s | %-30s | %-30s\n",
                "ID", "Name", "Age", "Medical History", "Allergies");
        System.out.println("=================================================================================================");

        for (Patient p : list) {

            String history = limit(p.formatList(p.getMedicalHistory()), 30);
            String allergy = limit(p.formatList(p.getAllergies()), 30);

            System.out.printf("%-6s | %-15s | %-4d | %-30s | %-30s\n",
                    p.getPatientID(),
                    limit(p.getPatientName(), 15),
                    p.getAge(),
                    history,
                    allergy
            );
        }

        System.out.println("=================================================================================================");
    }   
    
    // DISPLAY SINGLE PATIENT
    private void displaySinglePatient(Patient p) {
        if (p == null) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.println("\nCurrent Patient Data:");
        System.out.println("=================================================================================================");
        System.out.printf("%-6s | %-15s | %-4s | %-30s | %-30s\n",
                "ID", "Name", "Age", "Medical History", "Allergies");
        System.out.println("=================================================================================================");

        String history = limit(p.formatList(p.getMedicalHistory()), 30);
        String allergy = limit(p.formatList(p.getAllergies()), 30);

        System.out.printf("%-6s | %-15s | %-4d | %-30s | %-30s\n",
                p.getPatientID(),
                limit(p.getPatientName(), 15),
                p.getAge(),
                history,
                allergy
        );

        System.out.println("=================================================================================================");
    }    

    // PATIENT MENU
    public void start() {
        int choice;
        do {
            displayMenu();
            choice = inputInt("Enter choice: ");

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
    }

    // CREATE
    private void addPatient() {
        System.out.println("\n--- Add Patient ---");

        String id = patientRepo.generatePatientID();
  
        String name = inputWithMaxLength("Name: ", 20, false);
        name = Utilities.capitalizeWords(name); 
        
        LocalDate birthDate = addBirthDate();

        var history = new ADT.List<String>();
        System.out.print("Has medical history? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            while (true) {
                String h = inputWithMaxLength("Enter 'done' to stop: ", 30, true);
                if (h.equalsIgnoreCase("done")) break;
                if (!h.isEmpty()) {
                    h = Utilities.capitalizeWords(h);
                    history.add(h);
                }
            }
        }

        var allergy = new ADT.List<String>();
        System.out.print("Has allergy? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            while (true) {
                String a = inputWithMaxLength("Enter 'done' to stop: ", 15, true);
                if (a.equalsIgnoreCase("done")) break;
                if (!a.isEmpty()) {
                    a = Utilities.capitalizeWords(a);
                    allergy.add(a);
                }
            }
        }

        patientRepo.create(new Patient(id, name, birthDate, history, allergy));
        System.out.println("Patient added.");
    }

    // UPDATE LIST
    private void managePatientList(String type, String id) {
        while (true) {
            Patient p = patientRepo.findById(id);

            System.out.println("\n--- " + type + " ---");
            System.out.println(
                    type.equals("Medical History")
                            ? p.formatList(p.getMedicalHistory())
                            : p.formatList(p.getAllergies())
            );

            System.out.println("Choice <1. Add  2. Update  3. Remove  4. Exit>");
            int choice = inputInt("Choose: ");

            if (choice == 4) break;

            boolean changed = false; // to determine display or not

            switch (choice) {
                case 1 -> {
                    String item = inputWithMaxLength("New: ", 30, false);
                    item = Utilities.capitalizeWords(item);
                    boolean ok = type.equals("Medical History")
                            ? patientRepo.addPatientMedicalHistory(id, item)
                            : patientRepo.addPatientAllergy(id, item);
                    System.out.println(ok ? "Added" : "Failed");
                    changed = ok;
                }
                case 2 -> {
                    String oldItem = inputString("Old data: ");
                    String newItem = inputWithMaxLength("New data: ", 30, false);
                    newItem = Utilities.capitalizeWords(newItem);
                    boolean ok = type.equals("Medical History")
                            ? patientRepo.updatePatientMedicalHistory(id, oldItem, newItem)
                            : patientRepo.updatePatientAllergy(id, oldItem, newItem);
                    System.out.println(ok ? "Updated" : "Not found");
                    changed = ok;
                }
                case 3 -> {
                    String item = inputString("Remove: ");
                    boolean ok = type.equals("Medical History")
                            ? patientRepo.removePatientMedicalHistory(id, item)
                            : patientRepo.removePatientAllergy(id, item);
                    System.out.println(ok ? "Removed" : "Not found");
                    changed = ok;
                }
                default -> System.out.println("Invalid.");
            }

            if (changed) {
                displaySinglePatient(p);
            }
        }
    }

    // READ and SEARCH
    private void viewAllPatients() {
        displayList(patientRepo.findAll());
    }

    private void searchById() {
        String id = inputString("ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null) {
            System.out.println("Not found.");
        } else {
            displayList(new ADT.List<>(java.util.List.of(p)));
        }
    }

    private void searchByName() {
        displayList(patientRepo.findByName(inputString("Name: ")));
    }

    private void searchPatientsWithAllergy() {
        displayList(patientRepo.findPatientsWithAllergy());
    }

    // UPDATE 
    private void updatePatient() {
        String id = inputString("Patient ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null) {
            System.out.println("Not found.");
            return;
        }

        System.out.println("\nBefore Update:");
        displaySinglePatient(p);

        String name = inputWithMaxLength("New name: ", 20, true);
        
        if (!name.isEmpty()) {
            name = Utilities.capitalizeWords(name);
            p.setPatientName(name);
        }

        p.setBirthDate(updateBirthDate(p.getBirthDate()));

        managePatientList("Medical History", id);
        managePatientList("Allergies", id);

        patientRepo.update(p);

        System.out.println("\nAfter Update:");
        displaySinglePatient(p); 
    }

    // DELETE
    private void deletePatient() {
        String id = inputString("ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null) {
            System.out.println("Not found.");
            return;
        }

        displaySinglePatient(p);

        System.out.print("Confirm delete (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            patientRepo.delete(p);
            System.out.println("Deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }
    
    // PRINT SORTED PATIENT 
    private void printAllPatientsSortedByName() {
        displayList(patientRepo.getPatientsSortedByName());
    }
    
    // REPORT
    private void generateReport() {
        String report = patientRepo.generatePatientReport();
        System.out.println(report);

        System.out.print("Export? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            Utilities.exportReportToFile(report, "PatientReport.txt");
        }
    }
}