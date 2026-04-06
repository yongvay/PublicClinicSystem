package Boundary;

import ADT.List;
import ADT.ListInterface;
import Control.AppointmentRepository;
import Control.PatientRepository;
import Control.DoctorRepository;
import Control.MedicineRepository;
import Entity.Appointment;
import Entity.Patient;
import Entity.Medicine;
import Entity.Doctor; 
import Utility.Utilities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * @author Ng Yong Vay
 */
public class AppointmentUI {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo; 
    private final DoctorRepository doctorRepo;   
    private final MedicineRepository medicineRepo; 

    public AppointmentUI(AppointmentRepository appointmentRepo, PatientRepository patientRepo, DoctorRepository doctorRepo, MedicineRepository medicineRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.medicineRepo = medicineRepo;
    }

    public void displayAppointmentMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n==========================================");
            System.out.println("          APPOINTMENT MANAGEMENT          ");
            System.out.println("==========================================");
            System.out.println("1. Book New Appointment");
            System.out.println("2. Process / Transfer / Discharge Patient");
            System.out.println("3. View Appointments");
            System.out.println("4. Delete / Cancel Appointment");
            System.out.println("0. Back to Main Menu");
            System.out.println("==========================================");
            System.out.print("Choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1 -> bookAppointment(scanner);
                case 2 -> processAppointment(scanner);
                case 3 -> viewAppointments();
                case 4 -> deleteAppointment(scanner);
            }
        } while (choice != 0);
    }

    private ListInterface<Medicine> selectMedicines(Scanner scanner) {
        ListInterface<Medicine> prescribedMeds = new List<>();
        System.out.print("Does the patient require medicine? (Y/N): ");
        if (scanner.nextLine().equalsIgnoreCase("Y")) {
            boolean addMore = true;
            while (addMore) {
                System.out.print("Enter Medicine ID (e.g., M001): ");
                String medId = scanner.nextLine();
                Medicine m = medicineRepo.findById(medId);
                
                if (m != null) {
                    if (m.getQuantityInStock() > 0) {
                        prescribedMeds.add(m);
                        System.out.println("Added: " + m.getName() + " to prescription.");
                    } else {
                        System.out.println("Error: " + m.getName() + " is currently OUT OF STOCK.");
                    }
                } else {
                    System.out.println("Error: Medicine ID not found.");
                }
                
                System.out.print("Assign another medicine? (Y/N): ");
                addMore = scanner.nextLine().equalsIgnoreCase("Y");
            }
        }
        return prescribedMeds;
    }

    // TABLE DISPLAY 
    private String limit(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
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
    
    private Patient handleQuickRegistration(Scanner scanner) {
        System.out.println("\n--- Quick Patient Registration ---");
        System.out.print("Enter Patient Name: ");
        String name = Utilities.capitalizeWords(scanner.nextLine().trim());
      
        if (name.isEmpty()) {
            System.out.println("Patient name cannot be empty!");
            System.out.print("Proceed with new registration? (Y/N): ");
            if (!scanner.nextLine().equalsIgnoreCase("Y")) {
                System.out.println("Registration cancelled.");
                return null;
            }            
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = null;

        while (true) {
            System.out.print("Enter Birth Date (dd/MM/yyyy): ");
            String dateStr = scanner.nextLine().trim();

            try {
                birthDate = LocalDate.parse(dateStr, fmt);

                if (birthDate.isAfter(LocalDate.now())) {
                    System.out.println("Error: Birth date cannot be in the future.");
                    continue;
                }

                int age = LocalDate.now().getYear() - birthDate.getYear();
                if (age < 0 || age > 120) {
                    System.out.println("Error: Age must be between 0 and 120.");
                    continue;
                }

                break; // if valid, exit loop

            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format. Please use dd/MM/yyyy.");
            }
        }
        ListInterface<String> historyList = new List<>();
        ListInterface<String> allergyList = new List<>();

        Patient patient = patientRepo.registerPatient(name, birthDate, historyList, allergyList);

        System.out.println("Success! New Patient ID: " + patient.getPatientID());
       
        displaySinglePatient(patient);

        return patient;
    }

    private Patient handleExistingPatient(Scanner scanner) {
        System.out.print("\nEnter Patient ID: ");
        String patientId = scanner.nextLine().trim();
        Patient patient = patientRepo.findById(patientId);
        if (patient == null) {
            System.out.println("Error: Patient ID [" + patientId + "] does not exist.");
            return null;
        }
        System.out.println("Patient Found: " + patient.getPatientName());
        
        displaySinglePatient(patient);
        return patient;
    }

    private void bookAppointment(Scanner scanner) {
        System.out.println("\n--- Booking Options ---");
        System.out.println("1. Register New Patient & Book");
        System.out.println("2. Book for Existing Patient");
        System.out.print("Choice: ");
        String ptChoice = scanner.nextLine().trim();

        Patient patient;
        switch (ptChoice) {
            case "1" -> patient = handleQuickRegistration(scanner);
            case "2" -> patient = handleExistingPatient(scanner);
            default -> {
                System.out.println("Invalid choice. Returning to menu.");
                return;
            }
        }

        if (patient == null) {
            System.out.println("Booking cancelled.");
            return;
        }

        ListInterface<Doctor> allDoctors = doctorRepo.findAll();
        if (allDoctors.isEmpty()) {
            System.out.println("Error: No doctors are currently registered in the system.");
            return;
        }

        ListInterface<String> specializations = new List<>();
        for (int i = 1; i <= allDoctors.getNumberOfEntries(); i++) {
            String spec = allDoctors.getEntry(i).getSpecialization();
            boolean exists = false;
            
            for (int j = 1; j <= specializations.getNumberOfEntries(); j++) {
                if (specializations.getEntry(j).equalsIgnoreCase(spec)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                specializations.add(spec);
            }
        }

        System.out.println("\n==========================================");
        System.out.println("         AVAILABLE SPECIALIZATIONS        ");
        System.out.println("==========================================");
        for (int i = 1; i <= specializations.getNumberOfEntries(); i++) {
            System.out.printf("  [%d] %s\n", i, specializations.getEntry(i));
        }
        System.out.println("==========================================");

        System.out.print("Select Specialization (Enter number): ");
        int specChoice;
        if (scanner.hasNextInt()) {
            specChoice = scanner.nextInt();
            scanner.nextLine(); 
        } else {
            System.out.println("Error: Invalid input. Please enter a number.");
            scanner.nextLine(); 
            return;
        }

        if (specChoice < 1 || specChoice > specializations.getNumberOfEntries()) {
            System.out.println("Error: Invalid selection. Returning to menu.");
            return;
        }

        String specialization = specializations.getEntry(specChoice);

        String resultMessage = appointmentRepo.bookAppointment(patient.getPatientID(), specialization);
        System.out.println("\n" + resultMessage);
    }

    private void processAppointment(Scanner scanner) {
        System.out.print("Enter Appointment ID to process/transfer/discharge: ");
        String appId = scanner.nextLine();
        
        Appointment targetApt = null;
        ListInterface<Appointment> list = appointmentRepo.getAllAppointments();
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Appointment a = list.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appId)) {
                targetApt = a;
                break;
            }
        }

        if (targetApt == null) {
            System.out.println("Error: Appointment ID [" + appId + "] not found.");
            return;
        }

        if (targetApt.getStatus().equalsIgnoreCase("Scheduled")) {
            System.out.println("Current Status: Scheduled for Consultation.");
            System.out.println("Does the patient need further admission?");
            System.out.print("Enter 'Treatment', 'Observation', or type 'None' if going home: ");
        } else if (targetApt.getStatus().equalsIgnoreCase("Admitted")) {
            System.out.println("Current Status: Admitted in " + targetApt.getRoom().getRoomType() + ".");
            System.out.println("Where is the admitted patient moving to?");
            System.out.print("Enter 'Treatment', 'Observation', or type 'None' to discharge them home: ");
        } else {
            System.out.println("Error: Cannot process. Appointment is already marked as '" + targetApt.getStatus() + "'.");
            return;
        }

        String targetRoomType = Utilities.capitalizeWords(scanner.nextLine());
        ListInterface<Medicine> meds = selectMedicines(scanner); 
        
        String resultMessage = "";
        if (targetApt.getStatus().equalsIgnoreCase("Scheduled")) {
            resultMessage = appointmentRepo.completeAppointment(appId, targetRoomType, meds);
        } else if (targetApt.getStatus().equalsIgnoreCase("Admitted")) {
            resultMessage = appointmentRepo.transferPatient(appId, targetRoomType, meds);
        }
        
        System.out.println("\n" + resultMessage);
    }

    private void viewAppointments() {
        System.out.println("\n--- All Appointments ---");
        ListInterface<Appointment> list = appointmentRepo.getAllAppointments();

        if (list.isEmpty()) {
            System.out.println("No appointments booked yet.");
        } else {
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                System.out.println(list.getEntry(i).toString());
            }
        }
    }

    private void deleteAppointment(Scanner scanner) {
        System.out.print("\nEnter Appointment ID to delete/cancel (e.g., A001): ");
        String appId = scanner.nextLine();
        
        System.out.print("Are you sure you want to delete appointment " + appId + "? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            String resultMessage = appointmentRepo.deleteAppointment(appId);
            System.out.println("\n" + resultMessage);
        } else {
            System.out.println("\nDeletion cancelled.");
        }
    }
}