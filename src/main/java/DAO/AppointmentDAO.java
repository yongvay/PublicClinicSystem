package DAO;

/**
 *
 * @author Ng Yong Vay
 */

import ADT.List;
import ADT.ListInterface;
import Control.DoctorRepository;
import Control.PatientRepository;
import Control.RoomRepository;
import Entity.Appointment;
import Entity.Doctor;
import Entity.Patient;
import Entity.Room;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class AppointmentDAO {
    // Adjust the path if Database folder is located elsewhere
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
                    appointment.getStatus()
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
    
    public ListInterface<Appointment> loadFromFile(PatientRepository pRepo, DoctorRepository dRepo, RoomRepository rRepo) {
        ListInterface<Appointment> loadedList = new List<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|"); // Split by delimiter
                
                if (data.length == 5) {
                    String aptId = data[0];
                    Patient patient = pRepo.findById(data[1]);
                    Doctor doctor = dRepo.findById(data[2]); // Assuming you add findById to DoctorRepo
                    Room room = rRepo.findById(data[3]);     // Assuming you add findById to RoomRepo
                    LocalDate date = LocalDate.parse(data[4]);
                    
                    // Only add if related entities exist
                    if (patient != null && doctor != null && room != null) {
                        loadedList.add(new Appointment(aptId, patient, doctor, room, date, "Scheduled"));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // It's normal if the file doesn't exist on the very first run
        } catch (IOException e) {
            System.err.println("Error: Failed to load appointments from file. " + e.getMessage());
        }
        
        return loadedList;
    }
}