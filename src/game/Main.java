package game;

import game.core.Game;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Extract native libraries from JAR and load them
        String nativePath = extractNativeLibraries();
        Game game = new Game();

        // Set the path so LWJGL can find the extracted .dll files
        System.setProperty("org.lwjgl.librarypath", nativePath);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            game.cleanup();
        }));

        // Start the game
        game.run();
    }

    private static String extractNativeLibraries() {
        // Get OS-specific native directory
        String osName = System.getProperty("os.name").toLowerCase();
        String nativeFolder = osName.contains("win") ? "natives-windows" :
                osName.contains("mac") ? "natives-macos" :
                        osName.contains("linux") ? "natives-linux" : "natives-unknown";

        // Create a temporary directory for the extracted natives
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "lwjgl-natives");
        if (!tempDir.exists()) tempDir.mkdir();

        // Extract native files from JAR to temp directory
        List<String> nativeFiles = Arrays.asList(
                "lwjgl.dll",
                "glfw.dll",
                "lwjgl_opengl.dll",
                "lwjgl_stb.dll"
        );
        for (String fileName : nativeFiles) {
            extractFile("/libs/native/" + fileName, new File(tempDir, fileName));
        }

        return tempDir.getAbsolutePath();
    }

    private static void extractFile(String resourcePath, File destination) {
        try (InputStream in = Main.class.getResourceAsStream(resourcePath);
             FileOutputStream out = new FileOutputStream(destination)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract native library: " + resourcePath, e);
        }
    }
}