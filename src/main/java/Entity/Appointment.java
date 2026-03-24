package Entity;

import ADT.List;
import ADT.ListInterface;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private String appointmentID;
    private Patient patient;
    private Doctor doctor;
    private Room room; // Can be null if Waitlisted
    private LocalDate appointmentDate;
    private String status;
    private ListInterface<Medicine> prescribedMedicines;

    public Appointment(String appointmentID, Patient patient, Doctor doctor, 
            Room room, LocalDate appointmentDate, String status) {
        this.appointmentID = appointmentID;
        this.patient = patient;
        this.doctor = doctor;
        this.room = room;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.prescribedMedicines = new List<>();
    }

    // Getters
    public String getAppointmentID() { return appointmentID; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public Room getRoom() { return room; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public String getStatus() { return status; }
    public ListInterface<Medicine> getPrescribedMedicines() { return prescribedMedicines; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setRoom(Room room) { this.room = room; } // Added for Waitlist logic
    public void setPrescribedMedicines(ListInterface<Medicine> meds) { this.prescribedMedicines = meds; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String roomDisp = (room != null) ? room.getRoomNumber() : "Waitlisted (None)";
        
        String medsDisplay = "None";
        if (prescribedMedicines != null && !prescribedMedicines.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            // Using your custom List getEntry(i) for complete safety
            for (int i = 1; i <= prescribedMedicines.getNumberOfEntries(); i++) {
                Medicine m = prescribedMedicines.getEntry(i);
                sb.append(m.getName()).append(", ");
            }
            medsDisplay = sb.substring(0, sb.length() - 2); // Remove last comma
        }
        
        return String.format("Apt ID: %s | Date: %s | Patient: %s | Doctor: %s | Room: %s | Status: %s | Meds: [%s]", 
                appointmentID, appointmentDate.format(fmt), patient.getPatientName(), doctor.getName(), roomDisp, status, medsDisplay);
    }   
}