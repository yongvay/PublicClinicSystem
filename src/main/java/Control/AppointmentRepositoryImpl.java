package Control;

import ADT.List;
import ADT.ListInterface;
import DAO.AppointmentDAO;
import Entity.Appointment;
import Entity.Doctor;
import Entity.Patient;
import Entity.Room;
import Entity.Medicine;
import java.time.LocalDate;

/**
 * @author Ng Yong Vay
 */
public class AppointmentRepositoryImpl implements AppointmentRepository {

    private ListInterface<Appointment> appointmentList;

    private PatientRepository patientRepo;
    private DoctorRepository doctorRepo;
    private RoomRepository roomRepo;
    private MedicineRepository medicineRepo; 
    private AppointmentDAO appointmentDAO;


    public AppointmentRepositoryImpl(PatientRepository patientRepo, DoctorRepository doctorRepo, RoomRepository roomRepo, MedicineRepository medicineRepo) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.roomRepo = roomRepo;
        this.medicineRepo = medicineRepo; 
        this.appointmentDAO = new AppointmentDAO();
        
        this.appointmentList = appointmentDAO.loadFromFile(patientRepo, doctorRepo, roomRepo, medicineRepo);
        if (this.appointmentList == null) {
            this.appointmentList = new List<>();
        }
    }

    private String generateAppointmentID() {
        int max = 0;
        if (appointmentList.isEmpty()) return "A001";

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            int num = Integer.parseInt(a.getAppointmentID().substring(1));
            if (num > max) max = num;
        }
        return "A" + String.format("%03d", max + 1);
    }
    
    private String processMedicines(Appointment apt, ListInterface<Medicine> meds) {
        if (meds != null && !meds.isEmpty()) {
            apt.setPrescribedMedicines(meds);
            for (int i = 1; i <= meds.getNumberOfEntries(); i++) {
                Medicine m = meds.getEntry(i);
                m.setQuantityInStock(m.getQuantityInStock() - 1);
                medicineRepo.update(m); 
            }
            return "\n[Inventory] Medicines successfully assigned and stock deducted.";
        }
        return "";
    }

    private String processWaitlist() {
        StringBuilder updates = new StringBuilder();
        
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment waitlistedApt = appointmentList.getEntry(i);
            
            if (waitlistedApt.getStatus().equalsIgnoreCase("Waitlisted")) {
                String requiredSpec = waitlistedApt.getDoctor().getSpecialization();
                
                boolean isDocAvailable = false;
                Doctor availableDoc = null;
                ListInterface<Doctor> availDocs = doctorRepo.findAllAvailableDoctors();
                
                for(int j = 1; j <= availDocs.getNumberOfEntries(); j++){
                    if(availDocs.getEntry(j).getSpecialization().equalsIgnoreCase(requiredSpec)){
                        isDocAvailable = true;
                        availableDoc = availDocs.getEntry(j); 
                        break; 
                    }
                }

                Room availRoom = null;
                ListInterface<Room> availRooms = roomRepo.findAllAvailableRooms();
                for(int j = 1; j <= availRooms.getNumberOfEntries(); j++){
                    if(availRooms.getEntry(j).getRoomType().equalsIgnoreCase("Consultation")){
                        availRoom = availRooms.getEntry(j);
                        break;
                    }
                }

                if (isDocAvailable && availRoom != null) {
                    availableDoc.setStatus(false);
                    doctorRepo.update(availableDoc);
                    
                    availRoom.setAvailable(false);
                    roomRepo.update(availRoom);
                    
                    waitlistedApt.setDoctor(availableDoc); 
                    waitlistedApt.setRoom(availRoom);
                    waitlistedApt.setStatus("Scheduled");
                    
                    updates.append("\n** WAITLIST UPDATE **\n");
                    updates.append("Waitlisted Appointment ").append(waitlistedApt.getAppointmentID())
                           .append(" automatically assigned to Doctor ").append(availableDoc.getName())
                           .append(" in Room ").append(availRoom.getRoomNumber()).append("\n");
                    updates.append("*********\n");
                }
            }
        }
        return updates.toString();
    }
    
    @Override
    public String bookAppointment(String patientId, String requiredSpecialization) {
        Patient patient = patientRepo.findById(patientId);
        if (patient == null) {
            return "Error: Booking Failed. Patient ID " + patientId + " not found.";
        }

        Doctor assignedDoctor = null;
        boolean doctorIsAvailable = false;

        ListInterface<Doctor> availableDoctors = doctorRepo.findAllAvailableDoctors();
        for (int i = 1; i <= availableDoctors.getNumberOfEntries(); i++) {
            Doctor d = availableDoctors.getEntry(i);
            if (d.getSpecialization().equalsIgnoreCase(requiredSpecialization)) {
                assignedDoctor = d;
                doctorIsAvailable = true;
                break;
            }
        }

        if (!doctorIsAvailable) {
            ListInterface<Doctor> allDoctors = doctorRepo.findAll(); 
            for (int i = 1; i <= allDoctors.getNumberOfEntries(); i++) {
                Doctor d = allDoctors.getEntry(i);
                if (d.getSpecialization().equalsIgnoreCase(requiredSpecialization)) {
                    assignedDoctor = d;
                    break;
                }
            }
        }

        if (assignedDoctor == null) {
            return "Error: Booking Failed. No doctor found for specialization '" + requiredSpecialization + "'.";
        }

        Room assignedRoom = null;
        ListInterface<Room> availableRooms = roomRepo.findAllAvailableRooms();
        for (int i = 1; i <= availableRooms.getNumberOfEntries(); i++) {
            Room r = availableRooms.getEntry(i);
            if (r.getRoomType().equalsIgnoreCase("Consultation")) {
                assignedRoom = r;
                break;
            }
        }

        String aptId = generateAppointmentID();
        Appointment newApt;
        String resultMessage;

        if (doctorIsAvailable && assignedRoom != null) {
            assignedDoctor.setStatus(false);
            doctorRepo.update(assignedDoctor); 
            
            assignedRoom.setAvailable(false);
            roomRepo.update(assignedRoom);
            
            newApt = new Appointment(aptId, patient, assignedDoctor, assignedRoom, LocalDate.now(), "Scheduled");
            resultMessage = "Success! Appointment successfully booked.\n" + newApt.toString();
        } else {
            newApt = new Appointment(aptId, patient, assignedDoctor, null, LocalDate.now(), "Waitlisted");
            
            if (!doctorIsAvailable) {
                resultMessage = "Notice: Doctor is currently busy. Patient placed on Waitlist.\n" + newApt.toString();
            } else {
                resultMessage = "Notice: Consultation rooms are full. Patient placed on Waitlist.\n" + newApt.toString();
            }
        }
        
        appointmentList.add(newApt);
        appointmentDAO.saveAppointment(newApt);
        return resultMessage;
    }

    @Override
    public String completeAppointment(String appointmentID, String targetRoomType, ListInterface<Medicine> prescribedMeds) {
        Appointment aptToComplete = null;
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appointmentID) && a.getStatus().equals("Scheduled")) {
                aptToComplete = a;
                break;
            }
        }

        if (aptToComplete == null) {
            return "Error: Scheduled Appointment not found or already completed.";
        }

        Room newRoom = null;
        boolean isAdmitted = false;
        
        if (targetRoomType != null && !targetRoomType.equalsIgnoreCase("None") && !targetRoomType.trim().isEmpty()) {
            ListInterface<Room> availableRooms = roomRepo.findAllAvailableRooms();
            for (int i = 1; i <= availableRooms.getNumberOfEntries(); i++) {
                Room r = availableRooms.getEntry(i);
                if (r.getRoomType().equalsIgnoreCase(targetRoomType)) {
                    newRoom = r; break;
                }
            }
            if (newRoom == null) {
                return "Error: Process Failed. No available rooms of type '" + targetRoomType + "'.";
            }
            isAdmitted = true;
        }

        String medMessage = processMedicines(aptToComplete, prescribedMeds);

        Doctor doc = aptToComplete.getDoctor();
        doc.setStatus(true); 
        doctorRepo.update(doc);

        Room freedConsultationRoom = aptToComplete.getRoom();
        if (freedConsultationRoom != null) {
            freedConsultationRoom.setAvailable(true); 
            roomRepo.update(freedConsultationRoom);
        }

        String mainMessage;
        if (isAdmitted) {
            newRoom.setAvailable(false);
            roomRepo.update(newRoom);
            aptToComplete.setRoom(newRoom);
            aptToComplete.setStatus("Admitted");
            mainMessage = "Success! Patient admitted to " + targetRoomType + " (Room: " + newRoom.getRoomNumber() + ").";
        } else {
            aptToComplete.setStatus("Completed");
            mainMessage = "Success! Appointment marked as completed. Patient discharged.";
        }

        String waitlistMessage = processWaitlist();

        appointmentDAO.saveAllToFile(appointmentList);
        
        return mainMessage + medMessage + waitlistMessage;
    }

    @Override
    public String transferPatient(String appointmentID, String targetRoomType, ListInterface<Medicine> prescribedMeds) {
        Appointment aptToTransfer = null;
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appointmentID) && a.getStatus().equalsIgnoreCase("Admitted")) {
                aptToTransfer = a;
                break;
            }
        }

        if (aptToTransfer == null) {
            return "Error: Admitted patient not found.";
        }

        String medMessage = processMedicines(aptToTransfer, prescribedMeds);
        Room currentRoom = aptToTransfer.getRoom();

        if (targetRoomType == null || targetRoomType.equalsIgnoreCase("None") || targetRoomType.trim().isEmpty()) {
            if (currentRoom != null) {
                currentRoom.setAvailable(true);
                roomRepo.update(currentRoom);
            }
            aptToTransfer.setStatus("Completed");
            appointmentDAO.saveAllToFile(appointmentList);
            return "Success! Patient discharged successfully from " + currentRoom.getRoomType() + "." + medMessage;
        }

        Room newRoom = null;
        ListInterface<Room> availableRooms = roomRepo.findAllAvailableRooms();
        for (int i = 1; i <= availableRooms.getNumberOfEntries(); i++) {
            Room r = availableRooms.getEntry(i);
            if (r.getRoomType().equalsIgnoreCase(targetRoomType)) {
                newRoom = r; break;
            }
        }

        if (newRoom == null) {
            return "Error: Transfer Failed. No rooms of type '" + targetRoomType + "'.";
        }

        if (currentRoom != null) {
            currentRoom.setAvailable(true);
            roomRepo.update(currentRoom);
        }

        newRoom.setAvailable(false);
        roomRepo.update(newRoom);

        aptToTransfer.setRoom(newRoom);
        appointmentDAO.saveAllToFile(appointmentList);
        return "Success! Patient transferred to " + targetRoomType + " (Room: " + newRoom.getRoomNumber() + ")." + medMessage;
    }

    @Override
    public ListInterface<Appointment> getAllAppointments() {
        return appointmentList;
    }
}