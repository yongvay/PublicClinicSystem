/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import ADT.List;
import ADT.ListInterface;
import Entity.Room;
import java.io.*;

public class RoomDAO {
    
    // UPDATED: Using forward slashes for cross-platform compatibility
    private static final String FILE_NAME = "src/main/java/Database/rooms.txt";
    private static final String DELIMITER = "\\|"; 
    private static final String SEPARATOR = "|";   

    public void saveToFile(ListInterface<Room> roomList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
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
        File file = new File(FILE_NAME);
        
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