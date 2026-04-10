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
 * @author All Members
 */
public class AppointmentUI {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final MedicineRepository medicineRepo;

    public AppointmentUI(AppointmentRepository appointmentRepo, PatientRepository patientRepo,
            DoctorRepository doctorRepo, MedicineRepository medicineRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.medicineRepo = medicineRepo;
    }

    // SAFE INPUT HELPERS
    private int inputInt(Scanner scanner, String prompt) {
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

    private boolean inputYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y"))
                return true;
            if (input.equalsIgnoreCase("N"))
                return false;

            System.out.println("Invalid input. Please enter Y or N.");
        }
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
            System.out.println("5. Generate Status Reports");
            System.out.println("6. Generate Medication Audit Reports");
            System.out.println("0. Back to Main Menu");
            System.out.println("==========================================");

            choice = inputInt(scanner, "Choice: ");

            switch (choice) {
                case 1 -> bookAppointment(scanner);
                case 2 -> processAppointment(scanner);
                case 3 -> viewAppointments();
                case 4 -> deleteAppointment(scanner);
                case 5 -> generateStatusReport(scanner);
                case 6 -> generateMedicationAuditReport(scanner);
                case 0 -> System.out.println("Returning...");
                default -> System.out.println("Invalid choice.");
            }

        } while (choice != 0);
    }

    // SELECT MEDICINES
    private ListInterface<Medicine> selectMedicines(Scanner scanner) {
        ListInterface<Medicine> prescribedMeds = new List<>();

        if (!inputYesNo(scanner, "Does the patient require medicine? (Y/N): ")) {
            return prescribedMeds;
        }

        boolean addMore = true;

        while (addMore) {

            // display all medicines
            ListInterface<Medicine> allMeds = medicineRepo.findAll();

            if (allMeds.isEmpty()) {
                System.out.println("No medicines available.");
                break;
            }

            System.out.println("\n========== AVAILABLE MEDICINES ==========");

            int displayIndex = 1;
            ListInterface<Medicine> availableList = new List<>();

            for (int i = 1; i <= allMeds.getNumberOfEntries(); i++) {
                Medicine m = allMeds.getEntry(i);

                if (m.getQuantityInStock() > 0) {
                    System.out.printf("[%d] %-5s | %-20s | Stock: %d\n",
                            displayIndex,
                            m.getMedicineID(),
                            m.getName(),
                            m.getQuantityInStock());
                    availableList.add(m);
                    displayIndex++;
                }
            }

            if (availableList.isEmpty()) {
                System.out.println("All medicines are out of stock.");
                break;
            }

            System.out.println("=========================================");
            System.out.print("Select Medicine (Enter number): ");

            String input = scanner.nextLine().trim();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice < 1 || choice > availableList.getNumberOfEntries()) {
                System.out.println("Invalid selection.");
                continue;
            }

            Medicine selected = availableList.getEntry(choice);

            selected.setQuantityInStock(selected.getQuantityInStock() - 1);
            medicineRepo.update(selected);

            // add to list
            prescribedMeds.add(selected);

            System.out.println("Added: " + selected.getName());

            System.out.println("Remaining Stock: " + selected.getQuantityInStock());

            addMore = inputYesNo(scanner, "Assign another medicine? (Y/N): ");
        }

        return prescribedMeds;
    }

    // TABLE DISPLAY
    private String limit(String text, int max) {
        if (text == null)
            return "";
        if (text.length() <= max)
            return text;
        return text.substring(0, max - 3) + "...";
    }

    private void displaySinglePatient(Patient p) {
        if (p == null) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.println("\nCurrent Patient Data:");
        System.out.println(
                "=================================================================================================");
        System.out.printf("%-6s | %-15s | %-4s | %-30s | %-30s\n",
                "ID", "Name", "Age", "Medical History", "Allergies");
        System.out.println(
                "=================================================================================================");

        String history = limit(p.formatList(p.getMedicalHistory()), 30);
        String allergy = limit(p.formatList(p.getAllergies()), 30);

        System.out.printf("%-6s | %-15s | %-4d | %-30s | %-30s\n",
                p.getPatientID(),
                limit(p.getPatientName(), 15),
                p.getAge(),
                history,
                allergy);

        System.out.println(
                "=================================================================================================");
    }

    // QUICK REGISTER NEW PATIENT FOR MAKING AN APPOINMENT
    private Patient handleQuickRegistration(Scanner scanner) {
        System.out.println("\n--- Quick Patient Registration ---");
        System.out.print("Enter Patient Name: ");
        String name = Utilities.capitalizeWords(scanner.nextLine().trim());

        if (name.isEmpty()) {
            System.out.println("Patient name cannot be empty!");
            if (!inputYesNo(scanner, "Proceed with new registration? (Y/N): ")) {
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

    // EXISTING PATIENT APPOINMENT BOOKING
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
        int ptChoice = inputInt(scanner, "Choice: ");

        Patient patient;
        switch (ptChoice) {
            case 1 -> patient = handleQuickRegistration(scanner);
            case 2 -> patient = handleExistingPatient(scanner);
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

        int specChoice = inputInt(scanner, "Select Specialization: ");

        if (specChoice < 1 || specChoice > specializations.getNumberOfEntries()) {
            System.out.println("Error: Invalid selection. Returning to menu.");
            return;
        }

        String specialization = specializations.getEntry(specChoice);

        String resultMessage = appointmentRepo.bookAppointment(patient.getPatientID(), specialization);
        System.out.println("\n" + resultMessage);
    }

    // PROCESS APPOINMENT
    private void processAppointment(Scanner scanner) {

        System.out.print("-- Process Appointment --\nEnter Appointment ID to process/transfer/discharge: ");
        String appId = scanner.nextLine().trim();

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

        // Display current patient status
        if (targetApt.getStatus().equalsIgnoreCase("Scheduled")) {
            System.out.println("\nCurrent Status: Scheduled for Consultation.");
            System.out.print("Enter 'Treatment' / 'Observation' for Admission, or 'None' for Complete Diagnosis: ");
        } else if (targetApt.getStatus().equalsIgnoreCase("Admitted")) {
            System.out.println("\nCurrent Status: Admitted in " + targetApt.getRoom().getRoomType() + ".");
            System.out.print(
                    "Enter 'Treatment' / 'Observation' for Transfer Patient, or 'None' for Complete Diagnosis: ");
        } else {
            System.out.println("Error: Cannot process. Appointment already '" + targetApt.getStatus() + "'.");
            return;
        }

        // input validation
        String targetRoomType;
        while (true) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Treatment")) {
                targetRoomType = "Treatment";
                break;
            } else if (input.equalsIgnoreCase("Observation")) {
                targetRoomType = "Observation";
                break;
            } else if (input.equalsIgnoreCase("None")) {
                targetRoomType = "None";
                break;
            } else {
                System.out.print("Invalid input. Please enter Treatment / Observation / None: ");
            }
        }
        ListInterface<Medicine> meds = selectMedicines(scanner);

        // avoid passing empty
        if (meds == null || meds.getNumberOfEntries() == 0) {
            meds = null;
        }

        String resultMessage = "";

        if (targetApt.getStatus().equalsIgnoreCase("Scheduled")) {
            resultMessage = appointmentRepo.completeAppointment(appId, targetRoomType, meds);
        } else if (targetApt.getStatus().equalsIgnoreCase("Admitted")) {
            resultMessage = appointmentRepo.transferPatient(appId, targetRoomType, meds);
        }

        System.out.println("\n" + resultMessage);
    }

    // READ APPOINTMENTS
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

    // DELETE APPOINMENT
    private void deleteAppointment(Scanner scanner) {

        System.out.print("\nEnter Appointment ID to cancel it (e.g., A001): ");
        String appId = scanner.nextLine().trim();

        Appointment target = null;
        ListInterface<Appointment> list = appointmentRepo.getAllAppointments();

        // find matched appoinment
        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            Appointment a = list.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appId)) {
                target = a;
                break;
            }
        }

        if (target == null) {
            System.out.println("Error: Appointment ID not found.");
            return;
        }

        System.out.println("\n========== APPOINTMENT DETAILS ==========");
        System.out.println(target.toString());
        System.out.println("=========================================");

        if (inputYesNo(scanner, "Are you sure you want to delete this appointment? (Y/N): ")) {
            String resultMessage = appointmentRepo.deleteAppointment(appId);
            System.out.println("\n" + resultMessage);
        } else {
            System.out.println("\nDeletion cancelled.");
        }
    }

    // REPORTS
    private void generateStatusReport(Scanner scanner) {
        // 1. Output Text Version to Console First
        String textReport = appointmentRepo.generateAppointmentStatusTextReport();
        System.out.println(textReport);

        if (!textReport.contains("No appointments available")) {
            // 2. Ask User if they want the HTML Version
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = appointmentRepo.generateAppointmentStatusHtmlReport();
                Utilities.exportReportToFile(htmlContent, "AppointmentStatusReport.html");
                System.out.println("SUCCESS: HTML Report saved as 'AppointmentStatusReport.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }

    private void generateMedicationAuditReport(Scanner scanner) {
        // 1. Output Text Version to Console First
        String textReport = appointmentRepo.generateMedicationAuditTextReport();
        System.out.println(textReport);

        if (!textReport.contains("No appointments available")) {
            // 2. Ask User if they want the HTML Version
            System.out.print("\nWould you like to export a visual HTML version of this report? (Y/N): ");
            String exportChoice = scanner.nextLine().trim();

            if (exportChoice.equalsIgnoreCase("Y")) {
                String htmlContent = appointmentRepo.generateMedicationAuditHtmlReport();
                Utilities.exportReportToFile(htmlContent, "MedicationAuditReport.html");
                System.out.println("SUCCESS: HTML Report saved as 'MedicationAuditReport.html' in GeneratedReports.");
            } else {
                System.out.println("Export skipped.");
            }
        }
    }
}