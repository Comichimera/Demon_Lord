package game.config;

import game.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {

    private static final Path FILE = Paths.get("settings.properties");

    public static final int[][] WINDOW_SIZES = { { 1280, 720 }, { 1600, 900 }, { 1920, 1080 } };

    public static float     masterVolume    = 1.0f;
    public static float     musicVolume     = 0.7f;
    public static float     sfxVolume       = 1.0f;
    public static boolean   fullscreen      = false;
    public static int       windowSize      = 0;
    public static boolean   vsync           = true;
    public static int       fov             = 60;
    public static int       turnSpeed       = 90;
    public static float     uiSize          = 1.0f;

    private Settings() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(FILE)) {
            p.load(r);
            masterVolume =  getFloat    (p, "masterVolume",     masterVolume, 0f, 1f);
            musicVolume =   getFloat    (p, "musicVolume",      musicVolume, 0f, 1f);
            sfxVolume =     getFloat    (p, "sfxVolume",        sfxVolume, 0f, 1f);
            fullscreen =    getBool     (p, "fullscreen",       fullscreen);
            windowSize =    getInt      (p, "windowSize",       windowSize, 0, WINDOW_SIZES.length - 1);
            vsync =         getBool     (p, "vsync",            vsync);
            fov =           getInt      (p, "fov",              fov, 50, 90);
            turnSpeed =     getInt      (p, "turnSpeed",        turnSpeed, 45, 240);
            uiSize =        getFloat    (p, "uiSize",           uiSize, 0.75f, 1.5f);
            Log.info("Loaded settings from " + FILE.toAbsolutePath());
        } catch (Exception e) {
            Log.warn("Couldn't read settings, using defaults", e);
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("masterVolume", String.valueOf(masterVolume));
        p.setProperty("musicVolume", String.valueOf(musicVolume));
        p.setProperty("sfxVolume", String.valueOf(sfxVolume));
        p.setProperty("fullscreen", String.valueOf(fullscreen));
        p.setProperty("windowSize", String.valueOf(windowSize));
        p.setProperty("vsync", String.valueOf(vsync));
        p.setProperty("fov", String.valueOf(fov));
        p.setProperty("turnSpeed", String.valueOf(turnSpeed));
        p.setProperty("uiSize", String.valueOf(uiSize));
        try (Writer w = Files.newBufferedWriter(FILE)) {
            p.store(w, "Demon Lord settings");
        } catch (IOException e) {
            Log.warn("Couldn't save settings", e);
        }
    }

    private static float getFloat(Properties p, String key, float def, float min, float max) {
        try {
            return Math.max(min, Math.min(max, Float.parseFloat(p.getProperty(key, String.valueOf(def)).trim())));
        } catch (NumberFormatException e) { return def; }
    }

    private static int getInt(Properties p, String key, int def, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(key, String.valueOf(def)).trim())));
        } catch (NumberFormatException e) { return def; }
    }

    private static boolean getBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
