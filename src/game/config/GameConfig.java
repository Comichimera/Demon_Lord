package game.config;

import java.io.InputStream;
import java.util.Properties;

public class GameConfig {
    public static final float TILE_SIZE      = 3.0f;
    public static final float ROOM_HEIGHT    = 3.0f;
    public static final float ROOM_HALF_SIZE = TILE_SIZE / 2.0f;

    public enum HealthSystem { CLASSIC, INJURIES }
    public static final HealthSystem healthSystem;

    static {
        Properties props = new Properties();
        try (InputStream is = GameConfig.class.getResourceAsStream("/data/game.properties")) {
            if (is != null) props.load(is);
        } catch (Exception ignored) {}
        String val = props.getProperty("health.system", "injuries").trim().toLowerCase();
        healthSystem = val.equals("classic") ? HealthSystem.CLASSIC : HealthSystem.INJURIES;
    }
}
