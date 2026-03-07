package Main;

/**
 *
 * @author Ng Yong Vay
 */
public class PublicClinicSystem {
    public static void main(String[] args) {
        
    //Boundary.MedicineUI medicineApp = new Boundary.MedicineUI(); // Test out medicine
         //medicineApp.start();
        
//    Boundary.DoctorUI doctorApp = new Boundary.DoctorUI();
//    doctorApp.start();
    
    Boundary.PatientUI patientTest = new Boundary.PatientUI();
    patientTest.start();    
        
    }
}