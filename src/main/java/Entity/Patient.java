/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author Tam Wan Jin
 */
public class Patient {    
    private final String patientID;
    private String patientName;
    private LocalDate birthDate;
    private String medicalHistory;
    private String allergies;
    
    //Constructor
        public Patient(String patientID, String patientName, LocalDate birthDate,String medicalHistory, String allergies) {
        this.patientID = patientID;
        this.patientName = patientName;
        this.birthDate = birthDate;
        this.medicalHistory = medicalHistory;
        this.allergies = allergies;
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

    public String getMedicalHistory() { 
        return medicalHistory;
    }

    public String getAllergies() { 
        return allergies;
    }     
    
    //Setters
    public void setPatientName(String name) { 
        this.patientName = name; 
    }

    public void setBirthDate(LocalDate birthDate) { 
        this.birthDate = birthDate; 
    }
    
    public void setMedicalHistory(String medicalHistory) { 
        this.medicalHistory = medicalHistory; 
    }

    public void setAllergies(String allergies) { 
        this.allergies = allergies; 
    }
    
    //Overriden Methods
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return String.format(
                "ID: %-6s | Name: %-20s | Birth Date: %-12s | Age: %-4d | History: %-30s | Allergies: %-15s",
                patientID,
                patientName,
                birthDate.format(formatter),
                getAge(),
                medicalHistory,
                allergies
        );
    }
}
