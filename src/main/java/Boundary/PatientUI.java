/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Boundary;
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

    public PatientUI() {
        patientRepo = new PatientRepositoryImpl();
        scanner = new Scanner(System.in);
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
            System.out.println("\n===== PATIENT MENU =====");
            System.out.println("1 Add Patient");
            System.out.println("2 View All Patients");
            System.out.println("3 Search Patient by ID");
            System.out.println("4 Search Patient by Name");
            System.out.println("5 Search Patient with Allergy");            
            System.out.println("6 Update Patient");
            System.out.println("7 Delete Patient");
            System.out.println("0 Exit");

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
}