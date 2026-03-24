package DAO;

import ADT.List;
import ADT.ListInterface;
import Control.DoctorRepository;
import Control.PatientRepository;
import Control.RoomRepository;
import Control.MedicineRepository;
import Entity.Appointment;
import Entity.Doctor;
import Entity.Patient;
import Entity.Room;
import Entity.Medicine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class AppointmentDAO {
    private static final String FILE_PATH = "src/main/java/Database/appointments.txt";

    // Helper to format medicines as "M001,M002"
    private String buildMedicineString(ListInterface<Medicine> meds) {
        if (meds == null || meds.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= meds.getNumberOfEntries(); i++) {
            sb.append(meds.getEntry(i).getMedicineID()).append(",");
        }
        return sb.substring(0, sb.length() - 1); // Remove trailing comma
    }

    public void saveAppointment(Appointment appointment) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            
            // Handle null room for waitlisted patients
            String roomNum = (appointment.getRoom() != null) ? appointment.getRoom().getRoomNumber() : "None";
            String medsStr = buildMedicineString(appointment.getPrescribedMedicines());
            
            String line = String.format("%s|%s|%s|%s|%s|%s|%s",
                    appointment.getAppointmentID(),
                    appointment.getPatient().getPatientID(), 
                    appointment.getDoctor().getDoctorID(),   
                    roomNum,   
                    appointment.getAppointmentDate().toString(),
                    appointment.getStatus(),
                    medsStr
            );
            
            writer.write(line);
            writer.newLine(); 
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save appointment to file. " + e.getMessage());
        }
    }

    public void saveAllToFile(ListInterface<Appointment> appointmentList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            
            for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
                Appointment appointment = appointmentList.getEntry(i);
                String roomNum = (appointment.getRoom() != null) ? appointment.getRoom().getRoomNumber() : "None";
                String medsStr = buildMedicineString(appointment.getPrescribedMedicines());
                
                String line = String.format("%s|%s|%s|%s|%s|%s|%s",
                        appointment.getAppointmentID(),
                        appointment.getPatient().getPatientID(),
                        appointment.getDoctor().getDoctorID(),
                        roomNum,
                        appointment.getAppointmentDate().toString(),
                        appointment.getStatus(),
                        medsStr
                );
                
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save all appointments to file. " + e.getMessage());
        }
    }
    
    // Updated to accept MedicineRepository
    public ListInterface<Appointment> loadFromFile(PatientRepository pRepo, DoctorRepository dRepo, RoomRepository rRepo, MedicineRepository mRepo) {
        ListInterface<Appointment> loadedList = new List<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|"); 
                
                if (data.length >= 6) { 
                    String aptId = data[0];
                    Patient patient = pRepo.findById(data[1]);
                    Doctor doctor = dRepo.findById(data[2]); 
                    
                    Room room = null;
                    if (!data[3].equals("None")) {
                        room = rRepo.findById(data[3]);
                    }
                    
                    LocalDate date = LocalDate.parse(data[4]);
                    String status = data[5];
                    
                    if (patient != null && doctor != null) {
                        Appointment apt = new Appointment(aptId, patient, doctor, room, date, status);
                        
                        // Load Medicines if the 7th column exists
                        if (data.length == 7 && !data[6].equals("None")) {
                            String[] medIds = data[6].split(",");
                            for (String mid : medIds) {
                                Medicine m = mRepo.findById(mid);
                                if (m != null) apt.getPrescribedMedicines().add(m);
                            }
                        }
                        
                        loadedList.add(apt);
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