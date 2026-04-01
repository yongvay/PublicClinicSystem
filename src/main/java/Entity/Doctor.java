package Entity;

/**
 * @author Xing Szen
 */
public class Doctor {
    
    private String doctorID;
    private String name;
    private String specialization;
    private String contactNum;
    private boolean isAvailable;

    public Doctor(String doctorID, String name, String specialization, String contactNum, boolean isAvailable) {
        this.doctorID = doctorID;
        this.name = name;
        this.specialization = specialization;
        this.contactNum = contactNum;
        this.isAvailable = isAvailable;
    }
    
    // GETTERS
    public String getDoctorID() { return doctorID; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getContactNum() { return contactNum; }
    public boolean getStatus() { return isAvailable; }

    // SETTERS
    public void setDoctorID(String doctorID) { this.doctorID = doctorID; }
    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }
    public void setStatus(boolean status) { this.isAvailable = status; }

    // OVERRIDDEN METHODS
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Doctor doctor = (Doctor) obj;
        return doctorID != null && doctorID.equalsIgnoreCase(doctor.doctorID);
    }

    @Override
    public String toString() {
        String statusStr = isAvailable ? "Available" : "Occupied";
        return String.format("ID: %-5s | Name: %-15s | Specialization: %-15s | Contact: %-12s | Status: %s", 
                doctorID, name, specialization, contactNum, statusStr);
    }
}