package game.config;

import game.util.Log;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class GameConfig {
    public static final float TILE_SIZE      = 3.0f;
    public static final float ROOM_HEIGHT    = 3.0f;
    public static final float ROOM_HALF_SIZE = TILE_SIZE / 2.0f;

    public enum HealthSystem { CLASSIC, INJURIES }
    public static final HealthSystem healthSystem;
    private static final Properties props = new Properties();

    private static final Set<String> LOGGED = new HashSet<>();

    static {
        try (InputStream is = GameConfig.class.getResourceAsStream("/data/game.properties")) {
            if (is != null) props.load(is);
            else Log.info("[config] game.properties not found, using defaults");
        } catch (Exception ignored) {}
        String val = props.getProperty("health.system", "injuries").trim().toLowerCase();
        healthSystem = val.equals("classic") ? HealthSystem.CLASSIC : HealthSystem.INJURIES;
    }

    public static String path(String key, String fallback) {
        String v = props.getProperty(key);
        String result = v != null ? v.trim() : fallback;
        if (LOGGED.add(key)) {
            String missing = GameConfig.class.getResource(result) == null ? " MISSING!" : "";
            Log.info("[config] " + key + " = " + result + (v != null ? " (game.properties)" : " (default)") + missing);
        }
        return result;
    }
}
