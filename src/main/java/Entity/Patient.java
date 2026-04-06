package Entity;

import ADT.ListInterface;
import java.time.LocalDate;
import java.time.Period;
/**
 * @author Tam Wan Jin
 */
public class Patient {    
    private final String patientID;
    private String patientName;
    private LocalDate birthDate;
    private ListInterface<String> medicalHistory;
    private ListInterface<String> allergies;
    
    //Constructor
        public Patient(String patientID, String patientName, LocalDate birthDate,ListInterface<String> historyList, ListInterface<String> allergyList) {
        this.patientID = patientID;
        this.patientName = patientName;
        this.birthDate = birthDate;
        this.medicalHistory = historyList;
        this.allergies = allergyList;
    }
    
    //Getters
    public String getPatientID() { 
        return patientID;
    } 
    public String getPatientName() { 
        return patientName;
    } 
    public LocalDate getBirthDate() { 
        return birthDate;
    }
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public ListInterface<String> getMedicalHistory() { 
        return medicalHistory;
    }

    public ListInterface<String> getAllergies() { 
        return allergies;
    }     
    
    //Setters
    public void setPatientName(String name) { 
        this.patientName = name; 
    }

    public void setBirthDate(LocalDate birthDate) { 
        this.birthDate = birthDate; 
    }
    
    public void setMedicalHistory(ListInterface<String> medicalHistory) { 
        this.medicalHistory = medicalHistory; 
    }

    public void setAllergies(ListInterface<String> allergies) { 
        this.allergies = allergies; 
    }
    
    public Patient(String patientID) {
        this.patientID = patientID;
    }
    
    public String formatList(ListInterface<String> list) {
        if (list == null || list.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= list.getNumberOfEntries(); i++) {
            sb.append(i)
              .append(". ")
              .append(list.getEntry(i));

            if (i < list.getNumberOfEntries()) {
                sb.append(" | ");
            }
        }

        return sb.toString();
    }   
    @Override
    public String toString() {
        return "ID: " + patientID + ", Name: " + patientName + ", DOB: " + birthDate + 
               "\nMedical History:\n" + formatList(medicalHistory) +
               "\nAllergies:\n" + formatList(allergies);
    }
}
