package md.utm.isa.ruleengine.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static File getFile(String fileName) {
        File file = new File(fileName);
        try {
            // Check if the file exists
            if (file.exists()) {
                System.out.println("File exists.");
            } else {
                // Create the file
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to create the file.");
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return file;
    }
}
