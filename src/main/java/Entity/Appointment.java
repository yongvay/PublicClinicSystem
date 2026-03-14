package Entity;

/**
 *
 * @author Ng Yong Vay
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private String appointmentID;
    private Patient patient;
    private Doctor doctor;
    private Room room;
    private LocalDate appointmentDate;
    private String status;

    public Appointment(String appointmentID, Patient patient, Doctor doctor, 
            Room room, LocalDate appointmentDate, String status) {
        this.appointmentID = appointmentID;
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.appointmentDate = appointmentDate;
        this.status = status;
    }

    // Getters
    public String getAppointmentID() { return appointmentID; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public Room getRoom() { return room; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Apt ID: %s | Date: %s | Patient: %s | Doctor: %s | Room: %s", 
                appointmentID, appointmentDate.format(fmt), patient.getPatientName(), doctor.getName(), room.getRoomNumber());
    }
}