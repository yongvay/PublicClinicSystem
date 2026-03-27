package Control;

import ADT.ListInterface;
import Entity.Appointment;
import Entity.Medicine;

/**
 * @author Ng Yong Vay
 */
public interface AppointmentRepository {
    
    String bookAppointment(String patientId, String requiredSpecialization);
    
    String completeAppointment(String appointmentID, String targetRoomType, ListInterface<Medicine> prescribedMeds);
    
    String transferPatient(String appointmentID, String targetRoomType, ListInterface<Medicine> prescribedMeds);
    
    String deleteAppointment(String appointmentID);
    
    ListInterface<Appointment> getAllAppointments();
}