package DAO;

/**
 *
 * @author Ng Yong Vay
 */

import ADT.ListInterface;
import Entity.Appointment;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Ng Yong Vay
 */
public class AppointmentDAO {
    // Adjust the path if your Database folder is located elsewhere
    private static final String FILE_PATH = "src/main/java/Database/appointments.txt";

    /**
     * Appends a new appointment record to the text file.
     * USED FOR: Booking a new appointment.
     */
    public void saveAppointment(Appointment appointment) {
        // The 'true' flag in FileWriter enables append mode
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            
            // Format the data using a delimiter (NOW INCLUDES STATUS)
            String line = String.format("%s|%s|%s|%s|%s|%s",
                    appointment.getAppointmentID(),
                    appointment.getPatient().getPatientID(), 
                    appointment.getDoctor().getDoctorID(),   
                    appointment.getRoom().getRoomNumber(),   
                    appointment.getAppointmentDate().toString(),
                    appointment.getStatus() // Added the 6th field here!
            );
            
            writer.write(line);
            writer.newLine(); 
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save appointment to file. " + e.getMessage());
        }
    }

    /**
     * Overwrites the entire text file with the provided list of appointments.
     * USED FOR: Modifying an existing appointment (e.g., completing it).
     */
    public void saveAllToFile(ListInterface<Appointment> appointmentList) {
        // Notice there is NO 'true' flag here. This means it will overwrite the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            
            for (Appointment appointment : appointmentList) {
                // Format the data using the delimiter "|"
                String line = String.format("%s|%s|%s|%s|%s|%s",
                        appointment.getAppointmentID(),
                        appointment.getPatient().getPatientID(),
                        appointment.getDoctor().getDoctorID(),
                        appointment.getRoom().getRoomNumber(),
                        appointment.getAppointmentDate().toString(),
                        appointment.getStatus() 
                );
                
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save all appointments to file. " + e.getMessage());
        }
    }
}