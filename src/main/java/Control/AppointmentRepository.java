package Control;

/**
 *
 * @author Ng Yong Vay
*/

import ADT.ListInterface;
import Entity.Appointment;

public interface AppointmentRepository {
    // The orchestration method
    boolean bookAppointment(String patientId, String requiredSpecialization);
    boolean completeAppointment(String appointmentID);
    
    ListInterface<Appointment> getAllAppointments();
}