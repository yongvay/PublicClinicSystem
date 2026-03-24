/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundary;
import ADT.List;
import Control.PatientRepository;
import Control.PatientRepositoryImpl;
import Entity.Patient;
import ADT.ListInterface;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
/**
 *
 * @author Tam Wan Jin
 */
public class PatientUI {

    private PatientRepository patientRepo;
    private Scanner scanner;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

   
    public PatientUI(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
        this.scanner = new Scanner(System.in);
    }

    private String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    private LocalDate getValidBirthDate(String input, DateTimeFormatter formatter) {
        try {
            LocalDate date = LocalDate.parse(input, formatter);
            if (date.isAfter(LocalDate.now())) {
                System.out.println("Birth date cannot be in the future. Please Enter Again.");
                return null;
            }
            return date;
        } 
        catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Example: 01/09/2005. Please Enter Again.");
            return null;
        }
    }  
    
    private LocalDate addBirthDate() {
        while (true) {
            System.out.print("\nBirth Date (dd/MM/yyyy): ");
            String input = scanner.nextLine();

            LocalDate date = getValidBirthDate(input, formatter);
            if (date != null) {
                return date;
            }
        }
    }
    
    private LocalDate updateBirthDate(LocalDate currentDate) {
       System.out.print("Current Birth Date: " + currentDate.format(formatter)
               + "\nNew Birth Date (dd/MM/yyyy, press Enter to keep current): ");

       while (true) {
           String input = scanner.nextLine();

           if (input.isEmpty()) {
               return currentDate;
           }

           LocalDate date = getValidBirthDate(input, formatter);
           if (date != null) {
               return date;
           }
       }
    }   
    
    public void start() {
        int choice = -1;

        do {
            System.out.println("\n==========================================");
            System.out.println("       CLINIC SUBSYSTEM: PATIENT MENU    ");
            System.out.println("==========================================");
            System.out.println("1 Add Patient");
            System.out.println("2 View All Patients");
            System.out.println("3 Search Patient by ID");
            System.out.println("4 Search Patient by Name");
            System.out.println("5 Search Patient with Allergy");            
            System.out.println("6 Update Patient");
            System.out.println("7 Delete Patient");
            System.out.println("8. View Patient Report");
            System.out.println("0 Exit to Main Menu");
            System.out.println("==========================================");            

            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> addPatient();
                case 2 -> viewAllPatients();
                case 3 -> searchById();
                case 4 -> searchByName();
                case 5 -> searchPatientsWithAllergy();
                case 6 -> updatePatient();
                case 7 -> deletePatient();
                case 8 -> generatePatientReport();
                case 0 -> System.out.println("Exiting Patient Subsystem...");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);

    }

    public void addPatient() {
        System.out.println("\n--- Add New Patient ---");
        String id = patientRepo.generatePatientID();

        System.out.print("\nPatient Name: ");
        String name = scanner.nextLine();

        LocalDate birthDate = addBirthDate();

        System.out.print("Medical History: ");
        String history = scanner.nextLine();

        String allergies;
        while (true) {
            System.out.print("Does the patient have allergies? (Yes/No): ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("Yes")) {
                System.out.print("Enter allergy: ");
                allergies = scanner.nextLine();
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
   
    private void viewAllPatients() {
        System.out.println("\n--- All Registered Patients ---");
        ListInterface<Patient> list = patientRepo.findAll();
        displayList(list);
    }

    private void searchById() {
        System.out.print("Enter patient ID to search: ");
        String id = scanner.nextLine();
        Patient found = patientRepo.findById(id);

        if (found != null)
            System.out.println("Patient Found: \n" + found.toString());
        else
            System.out.println("Patient <" + id + "> Not Exist");
    }

    private void searchByName() {
        System.out.print("Enter Patient Name to search: ");
        String name = scanner.nextLine();
        ListInterface<Patient> results = patientRepo.findByName(name);

        if (results.isEmpty()) {
            System.out.println("Patient <" + name + "> Not Exist");
        } else {
            System.out.println("Search Results:");
            displayList(results);
        }
    }

    public void searchPatientsWithAllergy() {
        ListInterface<Patient> list = patientRepo.findPatientsWithAllergy();

        if (list.isEmpty()) {
            System.out.println("No patients with allergies.");
            return;
        }
        System.out.println("\nPatients with Allergies:");
        for (Patient p : list) {
            System.out.println(p);
        }
    }    

    private void updatePatient() {
        System.out.print("\nEnter Patient ID to update: ");
        String id = scanner.nextLine();

        Patient existing = patientRepo.findById(id);
        if (existing == null) {
            System.out.println("Patient not found, Failed to Update");
            return;
        }

        System.out.println("Current Details: " + existing.toString());
        System.out.println("Enter new details (press Enter to keep current value):");

        String name = inputString("New Name (press Enter to keep current): ");
        if (!name.isEmpty()) existing.setPatientName(name);

        existing.setBirthDate(updateBirthDate(existing.getBirthDate()));

        String history = inputString("New Medical History (press Enter to keep current): ");
        if (!history.isEmpty()) existing.setMedicalHistory(history);

        String allergies = inputString("New Allergies (press Enter to keep current): ");
        if (!allergies.isEmpty()) existing.setAllergies(allergies);

        patientRepo.update(existing);
        System.out.println("Patient updated.");
    }

    private void deletePatient() {

        System.out.print("Enter patient ID: ");
        String id = scanner.nextLine();

        Patient p = patientRepo.findById(id);

        if (p != null) {
            patientRepo.delete(p);
            System.out.println("Patient deleted.");
        }
    }
    
    // Iterates through and prints the list
    private void displayList(ListInterface<Patient> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Patient p : list) {
            System.out.println(p.toString());
        }
    }

    public void generatePatientReport() {

       ListInterface<Patient> list = patientRepo.findAll();
       if (list.isEmpty()) {
           System.out.println("No patient data available.");
           return;
       }
       int total = list.getNumberOfEntries();
       int totalAge = 0;
       int minAge = Integer.MAX_VALUE;
       int maxAge = Integer.MIN_VALUE;
       int allergyCount = 0;
       int noAllergyCount = 0;

       // Age group counters
       int youngCount = 0;   // 0-18
       int adultCount = 0;   // 19-40
       int seniorCount = 0;  // 41+

       // Allergy Lists 
       ListInterface<String> allergyNames = new List<>();
       ListInterface<Integer> allergyCounts = new List<>();

       for (int i = 1; i <= total; i++) {
           Patient p = list.getEntry(i);
           int age = p.getAge();

           totalAge += age;
           if (age < minAge) minAge = age;
           if (age > maxAge) maxAge = age;

           // Age Group
           if (age <= 18) youngCount++;
           else if (age <= 40) adultCount++;
           else seniorCount++;

           // Allergy
           String allergy = p.getAllergies();
           if (allergy.equalsIgnoreCase("None")) {
               noAllergyCount++;
           } else {
               allergyCount++;

               // 查找 allergyNames 是否已有
               boolean found = false;
               for (int j = 1; j <= allergyNames.getNumberOfEntries(); j++) {
                   if (allergyNames.getEntry(j).equalsIgnoreCase(allergy)) {
                       int count = allergyCounts.getEntry(j);
                       allergyCounts.replace(j, count + 1);
                       found = true;
                       break;
                   }
               }
               if (!found) {
                   allergyNames.add(allergy);
                   allergyCounts.add(1);
               }
           }
       }
       double avgAge = (double) totalAge / total;
       String time = java.time.LocalDateTime.now()
               .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

       // Print Out Report
       System.out.println("\n======================");
       System.out.println("=== PATIENT REPORT ===");
       System.out.println("======================");
       System.out.println("Generated At: " + time);
       System.out.println("Total Patients: " + total);

       System.out.println("\n--- Age Statistics ---");
       System.out.println("Average Age: " + String.format("%.2f", avgAge));
       System.out.println("Youngest: " + minAge);
       System.out.println("Oldest: " + maxAge);

       System.out.println("\n--- Age Group ---");
       System.out.println("0-18: " + youngCount);
       System.out.println("19-40: " + adultCount);
       System.out.println("41+: " + seniorCount);

       System.out.println("\n--- Allergy Statistics ---");
       System.out.println("With Allergy: " + allergyCount);
       System.out.println("Without Allergy: " + noAllergyCount);

       System.out.println("\n--- Top Allergies ---");
       if (allergyNames.isEmpty()) {
           System.out.println("None");
       } else {
           for (int k = 1; k <= allergyNames.getNumberOfEntries(); k++) {
               System.out.println(allergyNames.getEntry(k) + " : " + allergyCounts.getEntry(k));
           }
       }
       System.out.println("======================\n");
   }  
}