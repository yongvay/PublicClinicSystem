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
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();

        Patient patient = patientRepo.findById(patientId);

        if (patient == null) {
            System.out.println("Error: Patient ID [" + patientId + "] does not exist.");
            return; 
        }

        System.out.println("Patient Found: " + patient.getPatientName());

        System.out.print("Enter Required Specialization: ");
        String specialization = scanner.nextLine();

        if (!doctorRepo.specializationExists(specialization)) {
            System.out.println("Error: No doctors found for specialization: " + specialization);
            return;
        }

        // ECB FIX: Capture the string and let the Boundary do the printing
        String resultMessage = appointmentRepo.bookAppointment(patientId, specialization);
        System.out.println("\n" + resultMessage);
    }

    private void completeAppointment(Scanner scanner) {
        System.out.print("Enter Appointment ID to process: ");
        String appId = scanner.nextLine();
        
        System.out.println("Does the patient need further admission?");
        System.out.print("Enter 'Ward', 'ICU', or type 'None' if going home: ");
        String targetRoomType = scanner.nextLine();
        
        ListInterface<Medicine> meds = selectMedicines(scanner); 
        
        // ECB FIX: Capture the string and let the Boundary do the printing
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
        
        // ECB FIX: Capture the string and let the Boundary do the printing
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