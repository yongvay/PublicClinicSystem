package Control;

/**
 *
 * @author Ng Yong Vay
 */
import ADT.List;
import ADT.ListInterface;
import DAO.AppointmentDAO;
import Entity.Appointment;
import Entity.Doctor;
import Entity.Patient;
import Entity.Room;
import java.time.LocalDate;

public class AppointmentRepositoryImpl implements AppointmentRepository {

    private ListInterface<Appointment> appointmentList;

    // Injecting the other subsystems' Control classes
    private PatientRepository patientRepo;
    private DoctorRepository doctorRepo;
    private RoomRepository roomRepo;
    private AppointmentDAO appointmentDAO;

    private int appointmentCounter = 1;

    public AppointmentRepositoryImpl() {
        this.patientRepo = new PatientRepositoryImpl();
        this.doctorRepo = new DoctorRepositoryImpl();
        this.roomRepo = new RoomRepositoryImpl();
        this.appointmentDAO = new AppointmentDAO();
        
        // BUG FIX: Load existing appointments instead of starting with a blank list
        this.appointmentList = appointmentDAO.loadFromFile(patientRepo, doctorRepo, roomRepo);
        if (this.appointmentList == null) {
            this.appointmentList = new List<>();
        }
    }

    private String generateAppointmentID() {
        int max = 0;
        if (appointmentList.isEmpty()) {
            return "A001";
        }

        for (Appointment a : appointmentList) {
            int num = Integer.parseInt(a.getAppointmentID().substring(1));
            if (num > max) {
                max = num;
            }
        }
        return "A" + String.format("%03d", max + 1);
    }
    
    @Override
    public boolean bookAppointment(String patientId, String requiredSpecialization) {
        // 1. Check Patient Validity
        Patient patient = patientRepo.findById(patientId);
        if (patient == null) {
            System.err.println("Booking Failed: Patient ID " + patientId + " not found.");
            return false;
        }

        // 2. Check Doctor Availability based on specialization
        Doctor assignedDoctor = null;
        ListInterface<Doctor> availableDoctors = doctorRepo.findAllAvailableDoctors();
        for (Doctor d : availableDoctors) {
            if (d.getSpecialization().equalsIgnoreCase(requiredSpecialization)) {
                assignedDoctor = d;
                break;
            }
        }

        if (assignedDoctor == null) {
            System.err.println("Booking Failed: No available doctor found for specialization '" + requiredSpecialization + "'.");
            return false;
        }

        // 3. Check Room Availability (We just need any available Consultation room)
        Room assignedRoom = null;
        ListInterface<Room> availableRooms = roomRepo.findAllAvailableRooms();
        for (Room r : availableRooms) {
            if (r.getRoomType().equalsIgnoreCase("Consultation")) {
                assignedRoom = r;
                break;
            }
        }

        if (assignedRoom == null) {
            System.err.println("Booking Failed: No available consultation rooms.");
            return false;
        }

        // 4. If all checks pass, finalize the transaction
        // Update statuses to "Occupied" (false) to prevent double-booking
        assignedDoctor.setStatus(false);
        doctorRepo.update(assignedDoctor); 
        
        assignedRoom.setAvailable(false);
        roomRepo.update(assignedRoom);

        // BUG FIX: Use the dynamic ID generator here
        String aptId = generateAppointmentID();
        Appointment newApt = new Appointment(aptId, patient, assignedDoctor, assignedRoom, LocalDate.now(), "Scheduled");
        
        appointmentList.add(newApt);
        appointmentDAO.saveAppointment(newApt);
        
        System.out.println("Success! Appointment successfully booked.");
        System.out.println(newApt.toString());
        return true;
    }

    // In AppointmentRepositoryImpl.java
    @Override
    public boolean completeAppointment(String appointmentID) {
        // 1. Find the appointment
        Appointment aptToComplete = null;
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appointmentID) && a.getStatus().equals("Booked")) {
                aptToComplete = a;
                break;
            }
        }

        if (aptToComplete == null) {
            System.err.println("Appointment not found or already completed.");
            return false;
        }

        // 2. Free up the Doctor
        Doctor doc = aptToComplete.getDoctor();
        doc.setStatus(true); // Make available again
        doctorRepo.update(doc); // Persist doctor change

        // 3. Free up the Room
        Room room = aptToComplete.getRoom();
        room.setAvailable(true); // Make available again
        roomRepo.update(room); // Persist room change

        // 4. Update the Appointment Status
        aptToComplete.setStatus("Completed");

        // 5. Persist the updated appointment list 
        appointmentDAO.saveAllToFile(appointmentList);

        System.out.println("Appointment " + appointmentID + " marked as completed. Doctor and Room are now free.");
        return true;
    }

    @Override
    public ListInterface<Appointment> getAllAppointments() {
        return appointmentList;
    }
}