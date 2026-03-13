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
    
    ListInterface<Appointment> getAllAppointments();
}