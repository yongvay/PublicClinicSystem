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
 * @author All Members
 */
public class AppointmentRepositoryImpl implements AppointmentRepository {

    private ListInterface<Appointment> appointmentList;

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final RoomRepository roomRepo;
    private final MedicineRepository medicineRepo;
    private final AppointmentDAO appointmentDAO;

    public AppointmentRepositoryImpl(PatientRepository patientRepo, DoctorRepository doctorRepo,
            RoomRepository roomRepo, MedicineRepository medicineRepo) {
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
        if (appointmentList.isEmpty())
            return "A001";

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            int num = Integer.parseInt(a.getAppointmentID().substring(1));
            if (num > max)
                max = num;
        }
        return "A" + String.format("%03d", max + 1);
    }

    private String processMedicines(Appointment apt, ListInterface<Medicine> meds) {

        if (meds != null && !meds.isEmpty()) {

            // get old list
            ListInterface<Medicine> current = apt.getPrescribedMedicines();

            // if null, create and initialize new list
            if (current == null) {
                current = new List<>();
            }

            // append new medicine instaed of replace
            for (int i = 1; i <= meds.getNumberOfEntries(); i++) {
                Medicine m = meds.getEntry(i);
                current.add(m);
            }

            // set back the updated medicine into current list in appoinment
            apt.setPrescribedMedicines(current);

            return "\n[Inventory] Medicines added (merged) and stock deducted.";
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

                for (int j = 1; j <= availDocs.getNumberOfEntries(); j++) {
                    if (availDocs.getEntry(j).getSpecialization().equalsIgnoreCase(requiredSpec)) {
                        isDocAvailable = true;
                        availableDoc = availDocs.getEntry(j);
                        break;
                    }
                }

                Room availRoom = null;
                ListInterface<Room> availRooms = roomRepo.findAllAvailableRooms();
                for (int j = 1; j <= availRooms.getNumberOfEntries(); j++) {
                    if (availRooms.getEntry(j).getRoomType().equalsIgnoreCase("Consult")) {
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
            if (r.getRoomType().equalsIgnoreCase("Consult")) {
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
                resultMessage = "Notice: Consult rooms are full. Patient placed on Waitlist.\n" + newApt.toString();
            }
        }

        appointmentList.add(newApt);
        appointmentDAO.saveAppointment(newApt);
        return resultMessage;
    }

    @Override
    public String completeAppointment(String appointmentID, String targetRoomType,
            ListInterface<Medicine> prescribedMeds) {
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
                    newRoom = r;
                    break;
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

        Room freedConsultRoom = aptToComplete.getRoom();
        if (freedConsultRoom != null) {
            freedConsultRoom.setAvailable(true);
            roomRepo.update(freedConsultRoom);
        }

        String mainMessage;
        if (isAdmitted) {
            newRoom.setAvailable(false);
            roomRepo.update(newRoom);
            aptToComplete.setRoom(newRoom);
            aptToComplete.setStatus("Admitted");
            mainMessage = "Success! Patient admitted to " + targetRoomType + " (Room: " + newRoom.getRoomNumber()
                    + ").";
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
                newRoom = r;
                break;
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
        return "Success! Patient transferred to " + targetRoomType + " (Room: " + newRoom.getRoomNumber() + ")."
                + medMessage;
    }

    @Override
    public String deleteAppointment(String appointmentID) {
        Appointment aptToDelete = null;
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            if (a.getAppointmentID().equalsIgnoreCase(appointmentID)) {
                aptToDelete = a;
                break;
            }
        }

        if (aptToDelete == null) {
            return "Error: Appointment ID not found.";
        }

        if (aptToDelete.getStatus().equalsIgnoreCase("Scheduled")) {
            Doctor doc = aptToDelete.getDoctor();
            if (doc != null) {
                doc.setStatus(true);
                doctorRepo.update(doc);
            }
            Room room = aptToDelete.getRoom();
            if (room != null) {
                room.setAvailable(true);
                roomRepo.update(room);
            }
        } else if (aptToDelete.getStatus().equalsIgnoreCase("Admitted")) {
            Room room = aptToDelete.getRoom();
            if (room != null) {
                room.setAvailable(true);
                roomRepo.update(room);
            }
        }

        appointmentList.remove(aptToDelete);

        String waitlistMessage = processWaitlist();

        appointmentDAO.saveAllToFile(appointmentList);

        return "Success: Appointment " + appointmentID + " has been deleted/cancelled." + waitlistMessage;
    }

    @Override
    public ListInterface<Appointment> getAllAppointments() {
        return appointmentList;
    }

    // ==========================================
    // REPORT 1: APPOINTMENT STATUS & WAITLIST
    // ==========================================
    @Override
    public String generateAppointmentStatusTextReport() {
        if (appointmentList.isEmpty())
            return "No appointments available for the report.\n";

        int scheduled = 0, waitlisted = 0, admitted = 0, completed = 0;

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            String status = appointmentList.getEntry(i).getStatus();
            if (status.equalsIgnoreCase("Scheduled"))
                scheduled++;
            else if (status.equalsIgnoreCase("Waitlisted"))
                waitlisted++;
            else if (status.equalsIgnoreCase("Admitted"))
                admitted++;
            else if (status.equalsIgnoreCase("Completed"))
                completed++;
        }

        StringBuilder report = new StringBuilder();
        report.append("\n=================================================================================\n");
        report.append("                   DAILY APPOINTMENT STATUS & WAITLIST REPORT                    \n");
        report.append("=================================================================================\n");
        report.append(String.format("Total Appointments : %d\n", appointmentList.getNumberOfEntries()));
        report.append(String.format("Waitlisted (Urgent): %d | Scheduled: %d | Admitted: %d | Completed: %d\n",
                waitlisted, scheduled, admitted, completed));
        report.append("=================================================================================\n");
        report.append(String.format("| %-8s | %-12s | %-15s | %-19s | %-11s |\n", "Apt ID", "Date", "Patient Name",
                "Doctor", "Status"));
        report.append("---------------------------------------------------------------------------------\n");

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            report.append(String.format("| %-8s | %-12s | %-15s | %-19s | %-11s |\n",
                    a.getAppointmentID(), a.getAppointmentDate(), a.getPatient().getPatientName(),
                    a.getDoctor().getName(), a.getStatus()));
        }
        return report.toString();
    }

    @Override
    public String generateAppointmentStatusHtmlReport() {
        if (appointmentList.isEmpty())
            return "<h1>No Data Available</h1>";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Appointment Status Report</title>");
        html.append(
                "<style>body{font-family: Arial, sans-serif; padding: 20px;} table{width: 100%; border-collapse: collapse;} th, td{padding: 10px; border: 1px solid #ddd; text-align: left;} th{background-color: #2c3e50; color: white;} .Waitlisted{color: red; font-weight: bold;} .Scheduled{color: blue;} .Admitted{color: orange;} .Completed{color: green;}</style></head><body>");
        html.append("<h1>📋 Daily Appointment Status Report</h1>");
        html.append("<table><tr><th>Apt ID</th><th>Date</th><th>Patient Name</th><th>Doctor</th><th>Status</th></tr>");

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            html.append("<tr><td>").append(a.getAppointmentID()).append("</td>")
                    .append("<td>").append(a.getAppointmentDate()).append("</td>")
                    .append("<td>").append(a.getPatient().getPatientName()).append("</td>")
                    .append("<td>").append(a.getDoctor().getName()).append("</td>")
                    .append("<td class='").append(a.getStatus()).append("'>").append(a.getStatus())
                    .append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    // ==========================================
    // REPORT 2: PRESCRIBED MEDICATION AUDIT
    // ==========================================
    @Override
    public String generateMedicationAuditTextReport() {
        if (appointmentList.isEmpty())
            return "No appointments available for the report.\n";

        StringBuilder report = new StringBuilder();
        report.append("\n=======================================================================\n");
        report.append("                   PRESCRIBED MEDICATION AUDIT REPORT                  \n");
        report.append("=======================================================================\n");
        report.append(String.format("| %-8s | %-15s | %-35s |\n", "Apt ID", "Patient", "Prescribed Medicines"));
        report.append("-----------------------------------------------------------------------\n");

        int medsDispensed = 0;
        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            ListInterface<Medicine> meds = a.getPrescribedMedicines();

            String medString = "None";
            if (meds != null && !meds.isEmpty()) {
                medsDispensed++;
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j <= meds.getNumberOfEntries(); j++) {
                    sb.append(meds.getEntry(j).getName()).append(", ");
                }
                medString = sb.substring(0, sb.length() - 2);
            }

            report.append(String.format("| %-8s | %-15s | %-35s |\n",
                    a.getAppointmentID(), a.getPatient().getPatientName(), medString));
        }
        report.append("=======================================================================\n");
        report.append("Total Appointments with prescriptions: ").append(medsDispensed).append("\n");
        return report.toString();
    }

    @Override
    public String generateMedicationAuditHtmlReport() {
        if (appointmentList.isEmpty())
            return "<h1>No Data Available</h1>";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Medication Audit Report</title>");
        html.append(
                "<style>body{font-family: Arial, sans-serif; padding: 20px;} table{width: 100%; border-collapse: collapse;} th, td{padding: 10px; border: 1px solid #ddd; text-align: left;} th{background-color: #8e44ad; color: white;}</style></head><body>");
        html.append("<h1>💊 Prescribed Medication Audit Report</h1>");
        html.append("<table><tr><th>Apt ID</th><th>Patient Name</th><th>Prescribed Medicines</th></tr>");

        for (int i = 1; i <= appointmentList.getNumberOfEntries(); i++) {
            Appointment a = appointmentList.getEntry(i);
            ListInterface<Medicine> meds = a.getPrescribedMedicines();

            String medString = "<em>None</em>";
            if (meds != null && !meds.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j <= meds.getNumberOfEntries(); j++) {
                    sb.append(meds.getEntry(j).getName()).append(", ");
                }
                medString = sb.substring(0, sb.length() - 2);
            }

            html.append("<tr><td>").append(a.getAppointmentID()).append("</td>")
                    .append("<td>").append(a.getPatient().getPatientName()).append("</td>")
                    .append("<td>").append(medString).append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }
}