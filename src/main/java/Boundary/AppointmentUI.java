package Boundary;

/**
 *
 * @author Ng Yong Vay
 */
import Entity.Appointment;
import ADT.ListInterface;
import Control.AppointmentRepository;
import Control.PatientRepository;
import Control.DoctorRepository;
import Entity.Patient;
import java.util.Scanner;

public class AppointmentUI {

    private AppointmentRepository appointmentRepo;

    private final PatientRepository patientRepo; // Added to validate existence
    private final DoctorRepository doctorRepo;   // Added to validate specialization

    public AppointmentUI(AppointmentRepository appointmentRepo, PatientRepository patientRepo, DoctorRepository doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public void displayAppointmentMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Appointment Management ---");
            System.out.println("1. Book New Appointment");
            System.out.println("2. Complete Appointment");
            System.out.println("3. View Appointments");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 ->
                    bookAppointment(scanner);
                case 2 ->
                    completeAppointment(scanner);
                case 3 ->
                    viewAppointments();
            }
        } while (choice != 0);
    }

    private void bookAppointment(Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();

        // BOUNDARY VALIDATION: Invoking method on Control to check ADT content
        // FIX: Use your existing findById method instead of getPatientById
        Patient patient = patientRepo.findById(patientId);

        if (patient == null) {
            System.out.println("Error: Patient ID [" + patientId + "] does not exist.");
            return; // Early exit saves user time
        }

        System.out.println("Patient Found: " + patient.getPatientName());

        System.out.print("Enter Required Specialization: ");
        String specialization = scanner.nextLine();

        // VALIDATION: Ensure specialization exists in the system
        if (!doctorRepo.specializationExists(specialization)) {
            System.out.println("Error: No doctors found for specialization: " + specialization);
            return;
        }

        boolean success = appointmentRepo.bookAppointment(patientId, specialization);
        if (success) {
            System.out.println("Appointment booked successfully.");
        } else {
            System.out.println("Booking failed. (Possible cause: No available doctors/rooms).");
        }
    }

    private void completeAppointment(Scanner scanner) {
        System.out.print("Enter Appointment ID to mark complete: ");
        String appId = scanner.nextLine();
        if (appointmentRepo.completeAppointment(appId)) {
            System.out.println("Appointment marked as completed.");
        } else {
            System.out.println("Error: Appointment ID not found.");
        }
    }

    private void viewAppointments() {
        System.out.println("\n--- All Appointments ---");
        ListInterface<Appointment> list = appointmentRepo.getAllAppointments();

        if (list.isEmpty()) {
            System.out.println("No appointments booked yet.");
        } else {
            // Explicitly using your custom ADT methods
            for (int i = 1; i <= list.getNumberOfEntries(); i++) {
                System.out.println(list.getEntry(i).toString());
            }
        }
    }
}
