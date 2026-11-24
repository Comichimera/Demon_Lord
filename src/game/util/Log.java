package game.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static PrintWriter file;

    private Log() {}

    public static void init() {
        try {
            Path dir = Paths.get("logs");
            Files.createDirectories(dir);
            Path latest = dir.resolve("latest.log");
            if (Files.exists(latest)) {
                Files.move(latest, dir.resolve("previous.log"), StandardCopyOption.REPLACE_EXISTING);
            }

            file = new PrintWriter(Files.newBufferedWriter(latest, StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            Log.warn("[LOG] Could not open log file: " + e.getMessage());
        }
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> error("Crash in thread '" + thread.getName() + "'" + e));
    }

    public static void info(String msg)                 { write("INFO", msg, null); }
    public static void warn(String msg)                 { write("WARN", msg, null); }
    public static void warn(String msg, Throwable e)    { write("WARN", msg, e); }
    public static void error(String msg)                { write("ERROR", msg, null); }
    public static void error(String msg, Throwable e)   { write("ERROR", msg, e); }

    private static synchronized void write(String level, String msg, Throwable e) {
        String line = "[" + LocalTime.now().format(TIME) + "] [" + level + "] " + msg;
        PrintStream console = level.equals("INFO") ? System.out : System.err;
        console.println(line);
        if (e != null) e.printStackTrace(console);
        if (file != null) {
            file.println(line);
            if (e != null) e.printStackTrace(file);
        }
    }

}
