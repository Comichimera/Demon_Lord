package game.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameData {
    private static String version = "Unknown";

    static {
        loadVersion();
    }

    private static void loadVersion() {
        try (InputStream input = GameData.class.getResourceAsStream("/data/version.txt")) {
            if (input != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("version=")) {
                        version = line.substring(8).trim();
                        break;
                    }
                }
                System.out.println("Loaded version: " + version); // <-- ADD THIS LINE
            } else {
                System.err.println("Warning: version.txt not found in JAR. Defaulting to 'Unknown'.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load version info: " + e.getMessage());
        }
    }


    public static String getVersion() {
        return version;
    }
}
