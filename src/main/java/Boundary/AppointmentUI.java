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
            System.out.println("2. Process / Transfer / Discharge Patient");
            System.out.println("3. View Appointments");
            System.out.println("4. Delete / Cancel Appointment");
            System.out.println("0. Back to Main Menu");
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

    private void bookAppointment(Scanner scanner) {
        System.out.println("\n--- Booking Options ---");
        System.out.println("1. Register New Patient & Book");
        System.out.println("2. Book for Existing Patient");
        System.out.print("Choice: ");
        String ptChoice = scanner.nextLine();

        Patient patient = null;

        if (ptChoice.equals("1")) {
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

        System.out.print("\nEnter Required Specialization (e.g., Cardiology, General Practice): ");
        String specialization = scanner.nextLine();

        if (!doctorRepo.specializationExists(specialization)) {
            System.out.println("Error: No doctors found for specialization: " + specialization);
            return;
        }

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

        String targetRoomType = scanner.nextLine();
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