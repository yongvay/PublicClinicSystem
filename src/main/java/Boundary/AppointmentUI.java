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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author Ng Yong Vay
 */
public class AppointmentUI {

    private AppointmentRepository appointmentRepo;
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
            System.out.println("\n--- Appointment Management ---");
            System.out.println("1. Book New Appointment");
            System.out.println("2. Complete Appointment / Admit Patient");
            System.out.println("3. Transfer / Discharge Admitted Patient");
            System.out.println("4. View Appointments");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1 -> bookAppointment(scanner);
                case 2 -> completeAppointment(scanner);
                case 3 -> transferPatient(scanner);
                case 4 -> viewAppointments();
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

    private void bookAppointment(Scanner scanner) {
        System.out.println("\n--- Booking Options ---");
        System.out.println("1. Register New Patient & Book");
        System.out.println("2. Book for Existing Patient");
        System.out.print("Choice: ");
        String ptChoice = scanner.nextLine();

        Patient patient = null;

        if (ptChoice.equals("1")) {
            // --- NEW PATIENT QUICK REGISTRATION ---
            System.out.println("\n--- Quick Patient Registration ---");
            String newId = patientRepo.generatePatientID(); 
            
            System.out.print("Enter Patient Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter Birth Date (dd/MM/yyyy): ");
            String dateStr = scanner.nextLine();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(dateStr, fmt);
            } catch (Exception e) {
                System.out.println("Invalid date format! Returning to menu.");
                return;
            }
            
            System.out.print("Enter Medical History: ");
            String history = scanner.nextLine();
            
            System.out.print("Enter Allergies (or type 'None'): ");
            String allergies = scanner.nextLine();
            
            patient = new Patient(newId, name, birthDate, history, allergies);
            patientRepo.create(patient);
            
            System.out.println("Success! New Patient registered with ID: " + newId);

        } else if (ptChoice.equals("2")) {
            // --- EXISTING PATIENT SEARCH ---
            System.out.print("\nEnter Patient ID: ");
            String patientId = scanner.nextLine();
            patient = patientRepo.findById(patientId);
            
            if (patient == null) {
                System.out.println("Error: Patient ID [" + patientId + "] does not exist.");
                return;
            }
            System.out.println("Patient Found: " + patient.getPatientName());
            
        } else {
            System.out.println("Invalid choice. Returning to menu.");
            return;
        }

        // --- CONTINUE WITH APPOINTMENT BOOKING ---
        System.out.print("\nEnter Required Specialization (e.g., Cardiology, General Practice): ");
        String specialization = scanner.nextLine();

        if (!doctorRepo.specializationExists(specialization)) {
            System.out.println("Error: No doctors found for specialization: " + specialization);
            return;
        }

        String resultMessage = appointmentRepo.bookAppointment(patient.getPatientID(), specialization);
        System.out.println("\n" + resultMessage);
    }

    private void completeAppointment(Scanner scanner) {
        System.out.print("Enter Appointment ID to process: ");
        String appId = scanner.nextLine();
        
        System.out.println("Does the patient need further admission?");
        System.out.print("Enter 'Ward', 'ICU', or type 'None' if going home: ");
        String targetRoomType = scanner.nextLine();
        
        ListInterface<Medicine> meds = selectMedicines(scanner); 
        
        String resultMessage = appointmentRepo.completeAppointment(appId, targetRoomType, meds);
        System.out.println("\n" + resultMessage);
    }

    private void transferPatient(Scanner scanner) {
        System.out.print("Enter Appointment ID to transfer/discharge: ");
        String appId = scanner.nextLine();
        
        System.out.println("Where is the admitted patient moving to?");
        System.out.print("Enter 'ICU', 'Ward', or type 'None' to discharge them home: ");
        String targetRoomType = scanner.nextLine();
        
        ListInterface<Medicine> meds = selectMedicines(scanner); 
        
        String resultMessage = appointmentRepo.transferPatient(appId, targetRoomType, meds);
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
}