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
        Runtime.getRuntime().addShutdownHook(new Thread(game::cleanup));

        // Start the game
        game.run();
    }

    private static String extractNativeLibraries() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String nativeFolder =
                os.contains("win")   ? "natives-windows" :
                        os.contains("mac")   ? "natives-macos"   :
                                os.contains("linux") ? "natives-linux"   : "natives-unknown";

        // Pick correct filenames per OS
        List<String> files;
        if (os.contains("win")) {
            files = Arrays.asList("lwjgl.dll", "glfw.dll", "lwjgl_opengl.dll", "lwjgl_stb.dll");
        } else if (os.contains("linux")) {
            files = Arrays.asList("liblwjgl.so", "libglfw.so", "liblwjgl_opengl.so", "liblwjgl_stb.so");
        } else if (os.contains("mac")) {
            files = Arrays.asList("liblwjgl.dylib", "libglfw.dylib", "liblwjgl_opengl.dylib", "liblwjgl_stb.dylib");
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        // Optional: if you bundle separate folders per arch (x86_64/aarch64), include that here
        String base = "/libs/native/" + nativeFolder + "/"; // e.g., /libs/native/natives-linux/

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "lwjgl-natives-" + System.nanoTime());
        if (!tempDir.mkdirs()) throw new RuntimeException("Failed to create temp dir: " + tempDir);

        for (String name : files) {
            extractFile(base + name, new File(tempDir, name)); // your extractFile(resourcePath, targetFile)
        }

        // Point LWJGL at the extracted natives (LWJGL 3 reads this)
        System.setProperty("org.lwjgl.librarypath", tempDir.getAbsolutePath());

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