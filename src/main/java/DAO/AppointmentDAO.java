package DAO;

/**
 *
 * @author Ng Yong Vay
 */

import Entity.Appointment;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AppointmentDAO {
    // Adjust the path if your Database folder is located elsewhere
    private static final String FILE_PATH = "src/main/java/Database/appointments.txt";

    /**
     * Appends a new appointment record to the text file.
     */
    public void saveAppointment(Appointment appointment) {
        // Using try-with-resources to automatically close the writer
        // The 'true' flag in FileWriter enables append mode
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            
            // Format the data using a delimiter, e.g., AppointmentID|PatientID|DoctorID|RoomNo|Date
            String line = String.format("%s|%s|%s|%s|%s",
                    appointment.getAppointmentID(),
                    appointment.getPatient().getPatientID(), // Gets the ID from Patient Entity
                    appointment.getDoctor().getDoctorID(),   // Gets the ID from Doctor Entity
                    appointment.getRoom().getRoomNumber(),   // Gets the Room Number from Room Entity
                    appointment.getAppointmentDate().toString() // e.g., 2026-03-13
            );
            
            writer.write(line);
            writer.newLine(); // Move to the next line for future records
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save appointment to file. " + e.getMessage());
        }
    }
}