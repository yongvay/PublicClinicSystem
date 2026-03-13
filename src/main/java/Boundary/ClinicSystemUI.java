package Boundary;

import java.util.Scanner;

/**
 * Central Boundary class that ties all subsystems together.
 * @author Ng Yong Vay
*/

public class ClinicSystemUI {

    // Instantiate all sub-boundaries
    private DoctorUI doctorUI;
    private MedicineUI medicineUI;
    private PatientUI patientUI;
    private RoomUI roomUI;
    private AppointmentUI appointmentUI;
    private Scanner scanner;

    public ClinicSystemUI() {
        this.doctorUI = new DoctorUI();
        this.medicineUI = new MedicineUI();
        this.patientUI = new PatientUI();
        this.roomUI = new RoomUI();
        this.appointmentUI = new AppointmentUI();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice = -1;
        do {
            displayMainMenu();
            System.out.print("Enter your choice: ");
            
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                processChoice(choice);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear bad input
            }
        } while (choice != 0);
    }

    private void displayMainMenu() {
        System.out.println("\n==========================================");
        System.out.println("      TARUMT PUBLIC CLINIC SYSTEM         ");
        System.out.println("==========================================");
        System.out.println("1. Doctor Management Module");
        System.out.println("2. Medicine Management Module");
        System.out.println("3. Patient Management Module");
        System.out.println("4. Room Management Module");
        System.out.println("5. Appointment Booking Module");
        System.out.println("0. Exit System");
        System.out.println("==========================================");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: doctorUI.start(); break;
            case 2: medicineUI.start(); break;
            case 3: patientUI.start(); break;
            case 4: roomUI.start(); break;
            case 5: appointmentUI.start(); break; // ADD THIS
            case 0: System.out.println("Shutting down... Goodbye!"); break;
            default: System.out.println("Invalid choice.");
        }
    }
}