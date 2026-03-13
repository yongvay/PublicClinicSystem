package Main;

import Boundary.ClinicSystemUI;

/**
 * Main application entry point.
 * @author Ng Yong Vay
 */
public class PublicClinicSystem {
    public static void main(String[] args) {
        // Initialize the central UI portal and start the system
        ClinicSystemUI mainApp = new ClinicSystemUI();
        mainApp.start();
    }
}