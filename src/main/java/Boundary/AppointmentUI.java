package Boundary;

/**
 *
 * @author Ng Yong Vay
 */

import Control.AppointmentRepository;
import Control.AppointmentRepositoryImpl;
import Entity.Appointment;
import ADT.ListInterface;
import java.util.Scanner;

public class AppointmentUI {
    
    private AppointmentRepository appointmentRepo;
    private Scanner scanner;

    public AppointmentUI() {
        this.appointmentRepo = new AppointmentRepositoryImpl();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice = -1;
        do {
            System.out.println("\n==========================================");
            System.out.println("     CLINIC SUBSYSTEM: APPOINTMENT MENU   ");
            System.out.println("==========================================");
            System.out.println("1. Book a New Appointment");
            System.out.println("2. View All Appointments");
            System.out.println("0. Exit to Main Menu");
            System.out.println("==========================================");
            System.out.print("Enter your choice: ");
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
                
                switch (choice) {
                    case 1: bookAppointment(); break;
                    case 2: viewAppointments(); break;
                    case 0: System.out.println("Exiting Appointment Subsystem..."); break;
                    default: System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("Invalid input.");
                scanner.nextLine();
            }
        } while (choice != 0);
    }

    private void bookAppointment() {
        System.out.println("\n--- Book Appointment ---");
        System.out.print("Enter Registered Patient ID (e.g., P001): ");
        String patientId = scanner.nextLine();
        
        System.out.print("Enter Required Specialization (e.g., General Practice, Cardiology): ");
        String spec = scanner.nextLine();
        
        // The Boundary passes raw input to the Control. It does not validate entities itself.
        appointmentRepo.bookAppointment(patientId, spec);
    }

    private void viewAppointments() {
        System.out.println("\n--- All Appointments ---");
        ListInterface<Appointment> list = appointmentRepo.getAllAppointments();
        if (list.isEmpty()) {
            System.out.println("No appointments booked yet.");
        } else {
            for (Appointment a : list) {
                System.out.println(a.toString());
            }
        }
    }
}