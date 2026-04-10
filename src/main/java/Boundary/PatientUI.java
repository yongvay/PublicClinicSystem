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
            String input = inputString("New Birth Date [dd/MM/yyyy] (Enter to skip): ");
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
    private void displayPatient(Patient p) {
        ListInterface<String> historyList = p.getMedicalHistory();
        ListInterface<String> allergyList = p.getAllergies();

        int maxRows = Math.max(
                historyList.getNumberOfEntries(),
                allergyList.getNumberOfEntries()
        );

        if (maxRows == 0) maxRows = 1;

        for (int i = 1; i <= maxRows; i++) {

            String history = "";
            String allergy = "";

            if (historyList.isEmpty() && i == 1) {
                history = "None";
            } else if (i <= historyList.getNumberOfEntries()) {
                history = i + ". " + historyList.getEntry(i);
            }

            if (allergyList.isEmpty() && i == 1) {
                allergy = "None";
            } else if (i <= allergyList.getNumberOfEntries()) {
                allergy = i + ". " + allergyList.getEntry(i);
            }

            if (i == 1) {
                System.out.printf("%-6s | %-15s | %-4d | %-30s | %-30s\n",
                        p.getPatientID(),
                        limit(p.getPatientName(), 15),
                        p.getAge(),
                        limit(history, 30),
                        limit(allergy, 30)
                );
            } else {
                System.out.printf("%-6s | %-15s | %-4s | %-30s | %-30s\n",
                        "",
                        "",
                        "",
                        limit(history, 30),
                        limit(allergy, 30)
                );
            }
        }

        System.out.println("-------------------------------------------------------------------------------------------------");
    }
    
    private void displayList(ListInterface<Patient> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println("=================================================================================================");
        System.out.printf("%-6s | %-15s | %-4s | %-30s | %-30s\n",
                "ID", "Name", "Age", "Medical History", "Allergies");
        System.out.println("=================================================================================================");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (Patient p : list) {
            displayPatient(p);
            
        }
        System.out.println("=================================================================================================");
    }
    
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
        System.out.println("-------------------------------------------------------------------------------------------------");
        displayPatient(p);

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
                case 9 -> generateAllergyReport();
                case 10 -> generatePatientAgeReport();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }

        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n========== PATIENT MENU ==========");
        System.out.println("1  Add Patient");
        System.out.println("2  View All Patients");
        System.out.println("3  Update Patient");
        System.out.println("4  Delete Patient");
        System.out.println("5  Search by ID");
        System.out.println("6  Search by Name");
        System.out.println("7  Search Patient with Allergy");
        System.out.println("8  View Patient List (A-Z)");
        System.out.println("9  Generate Patient Medical Report");
        System.out.println("10 Generate Patient Age Report");        
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
                String h = inputWithMaxLength("Enter 'done' to stop inserting data: ", 30, true);
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
                String a = inputWithMaxLength("Enter 'done' to stop inserting data: ", 30, true);
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
    private boolean modifyPatientListItem(Patient p, String type, int action) {
        // Get the list
        ListInterface<String> list;
        if (type.equals("Medical History")) {
            list = p.getMedicalHistory();
        } else {
            list = p.getAllergies();
        }

        if (action == 1) { // Add
            String item = inputWithMaxLength("New: ", 30, false);
            item = Utilities.capitalizeWords(item);

            boolean ok = type.equals("Medical History")
                    ? patientRepo.addPatientMedicalHistory(p.getPatientID(), item)
                    : patientRepo.addPatientAllergy(p.getPatientID(), item);

            System.out.println(ok ? "Added" : "Failed");
            return ok;

        } else { // Update or Remove
            int size = list.getNumberOfEntries();
            if (size == 0) {
                System.out.println("No data to " + (action == 2 ? "update." : "remove."));
                return false;
            }

            int index = inputInt("Select index (1-" + size + ") to update or remove: ");
            if (index < 1 || index > size) {
                System.out.println("Invalid index.");
                return false;
            }

            String selectedItem = list.getEntry(index);
            System.out.println("Selected: " + selectedItem);

            if (action == 2) { // Update
                String newItem = inputWithMaxLength("New data: ", 30, false);
                newItem = Utilities.capitalizeWords(newItem);
                System.out.print("Confirm update (Y/N): ");
                if (scanner.nextLine().equalsIgnoreCase("Y")) {
                    boolean ok = type.equals("Medical History")
                            ? patientRepo.updatePatientMedicalHistory(p.getPatientID(), selectedItem, newItem)
                            : patientRepo.updatePatientAllergy(p.getPatientID(), selectedItem, newItem);
                    System.out.println(ok ? "Updated" : "Failed");
                    return ok;
                }
                System.out.println("Cancelled.");
                return false;

            } else { // Remove
                System.out.print("Confirm remove (Y/N): ");
                if (scanner.nextLine().equalsIgnoreCase("Y")) {
                    boolean ok = type.equals("Medical History")
                            ? patientRepo.removePatientMedicalHistory(p.getPatientID(), selectedItem)
                            : patientRepo.removePatientAllergy(p.getPatientID(), selectedItem);
                    System.out.println(ok ? "Removed" : "Failed");
                    return ok;
                }
                System.out.println("Cancelled.");
                return false;
            }
        }
    }   
    
    private void managePatientList(String type, String id) {
        Patient p = patientRepo.findById(id);
        if (p == null) return;

        while (true) {
            System.out.println("\n--- " + type + " ---");
            // Display Current List
            ListInterface<String> list = type.equals("Medical History") ? p.getMedicalHistory() : p.getAllergies();
            System.out.println(p.formatList(list));

            System.out.println("Choice <1. Add  2. Update  3. Remove  0. Exit>");
            int choice = inputInt("Choose: ");
            if (choice == 0) break;

            boolean changed = modifyPatientListItem(p, type, choice);
            if (changed) displaySinglePatient(p);
        }
    }

    // READ and SEARCH
    private void viewAllPatients() {
        System.out.println("\n--- View All Patient ---");
        displayList(patientRepo.findAll());
    }

    private void searchById() {
        System.out.println("\n-- Search Patient by ID --");
        String id = inputString("ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null) {
            System.out.println("Not found.");
        } else {
            displayList(new ADT.List<>(java.util.List.of(p)));
        }
    }

    private void searchByName() {
        System.out.println("\n-- Search Patient by Name --");
        displayList(patientRepo.findByName(inputString("Name: ")));
    }

    private void searchPatientsWithAllergy() {
        System.out.println("\n-- Search Patient with Allergy --");
        displayList(patientRepo.findPatientsWithAllergy());
    }

    // UPDATE 
    private void updatePatient() {
        System.out.println("\n--- Update Patient ---");
        String id = inputString("Patient ID: ");
        Patient p = patientRepo.findById(id);

        if (p == null) {
            System.out.println("Not found.");
            return;
        }

        System.out.println("\nBefore Update:");
        displaySinglePatient(p);

        String name = inputWithMaxLength("New name (Enter to Skip): ", 20, true);
        
        if (!name.isEmpty()) {
            name = Utilities.capitalizeWords(name);
            p.setPatientName(name);
        }

        p.setBirthDate(updateBirthDate(p.getBirthDate()));

        managePatientList("Medical History", id);
        managePatientList("Allergies", id);

        patientRepo.update(p);

        System.out.println("\n Patient Updated!!");
        displaySinglePatient(p); 
    }

    // DELETE
    private void deletePatient() {
        System.out.println("\n--- Delete Patient ---");
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
        System.out.println("\n--- View Patient List (A-Z) ---");
        displayList(patientRepo.getPatientsSortedByName());
    }
    
    // REPORT
    private void generateAllergyReport() {
        String report = patientRepo.generatePatientAllergyReport();
        System.out.println(report);

        System.out.print("Export? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            Utilities.exportReportToFile(report, "PatientAllergyReport.txt");
        }
    }
    
    private void generatePatientAgeReport() {
        String report = patientRepo.generatePatientAgeReport();
        System.out.println(report);

        System.out.print("Export? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            Utilities.exportReportToFile(report, "PatientAgeReport.txt");
        }
    }    
    
}