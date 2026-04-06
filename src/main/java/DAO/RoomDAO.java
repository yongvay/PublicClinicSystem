package DAO;

import ADT.List;
import ADT.ListInterface;
import Entity.Room;
import java.io.*;

public class RoomDAO {
    
    private static final String DELIMITER = "\\|"; 
    private static final String SEPARATOR = "|";   

    // UPDATED: Helper method to safely resolve file paths regardless of run environment
    private String getFilePath() {
        File file = new File("src/main/java/Database/rooms.txt");
        if (file.exists() || file.getParentFile().exists()) {
            return "src/main/java/Database/rooms.txt";
        }
        // Fallback for execution outside NetBeans
        return "rooms.txt"; 
    }

    public void saveToFile(ListInterface<Room> roomList) {
        String filePath = getFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Room r : roomList) {
                String line = r.getRoomNumber() + SEPARATOR +
                              r.getRoomType() + SEPARATOR +
                              r.isAvailable(); 
                writer.write(line);
                writer.newLine(); 
            }
        } catch (IOException e) {
            System.err.println("Critical Error: Unable to save data to file -> " + e.getMessage());
        }
    }

    public ListInterface<Room> loadFromFile() {
        ListInterface<Room> loadedList = new List<>();
        String filePath = getFilePath();
        File file = new File(filePath);
        
        if (!file.exists()) {
            return loadedList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length == 3) {
                    Room r = new Room(
                        parts[0], 
                        parts[1], 
                        Boolean.parseBoolean(parts[2]) 
                    );
                    loadedList.add(r);
                }
            }
        } catch (IOException e) {
            System.err.println("Critical Error: File corruption or read failure -> " + e.getMessage());
        }
        
        return loadedList; 
    }
}