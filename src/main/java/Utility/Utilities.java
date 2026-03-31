package Utility;

import java.util.Scanner;

public class Utilities {
    private static final Scanner scanner = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public static void printHeader(String title) {
        System.out.println("\n--- " + title.toUpperCase() + " ---");
    }

    // Exports the given report content to a text file with the specified file name
    public static void exportReportToFile(String reportContent, String fileName) {
        // 1. Define the RELATIVE folder path inside your project directory
        String folderName = "src\\main\\java\\GeneratedReports\\";
        
        // Use File object to handle directory creation safely
        java.io.File directory = new java.io.File(folderName);
        
        // 2. If the folder doesn't exist, create it dynamically
        if (!directory.exists()) {
            directory.mkdir(); 
        }

        // 3. Construct the full relative path using the provided fileName
        java.io.File file = new java.io.File(directory, fileName);

        // 4. Write the file
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(file))) {
            out.println(reportContent);
            System.out.println("Success! Report exported to: " + file.getPath());
        } catch (java.io.IOException e) {
            System.out.println("Error: Failed to export report. " + e.getMessage());
        }
    }
}