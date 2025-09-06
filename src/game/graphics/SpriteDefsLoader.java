package game.graphics;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads enemy sprite definitions (front/side/back texture paths + size in tiles).
 * Supports:
 *  - Direct load from a specific JSON resource path, OR
 *  - Per-level load via loadForMap(mapJsonPath), which overlays:
 *      1) optional base:   "/data/map/spritedefs.json"
 *      2) optional per-map: "<dir-of-map>/spritedefs.json"
 */
public final class SpriteDefsLoader {

    public static final class SpriteDef {
        public final String frontPath;
        public final String sidePath;
        public final String backPath;
        public final float widthTiles;
        public final float heightTiles;

        SpriteDef(String frontPath, String sidePath, String backPath, float widthTiles, float heightTiles) {
            this.frontPath = frontPath;
            this.sidePath = sidePath;
            this.backPath = backPath;
            this.widthTiles = widthTiles;
            this.heightTiles = heightTiles;
        }
    }

    private final Map<String, SpriteDef> byType = new HashMap<>();

    /** Load from a single JSON resource path (kept for compatibility). */
    public SpriteDefsLoader(String resourcePath) {
        try (InputStream in = resource(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing spritedefs at " + resourcePath);
            }
            JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in, StandardCharsets.UTF_8)));
            putAll(byType, root);
        } catch (Exception e) {
            throw new RuntimeException("Failed loading spritedefs: " + e.getMessage(), e);
        }
    }

    /** Private ctor used by the per-map factory once definitions are merged. */
    private SpriteDefsLoader(Map<String, SpriteDef> prebuilt) {
        this.byType.putAll(prebuilt);
    }

    /**
     * Per-level loader mirroring tile definition behavior:
     *  1) Try base "/data/map/spritedefs.json" (optional).
     *  2) Overlay with "<dir-of-map>/spritedefs.json" (optional).
     * @param mapJsonPath the path to the current level's map.json (classpath or filesystem)
     */
    public static SpriteDefsLoader loadForMap(String mapJsonPath) {
        Map<String, SpriteDef> merged = new HashMap<>();

        // 1) Base (optional)
        try (InputStream in = resource("/data/map/spritedefs.json")) {
            if (in != null) {
                JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in, StandardCharsets.UTF_8)));
                putAll(merged, root);
            }
        } catch (Exception ignore) {
            // No base is fine.
        }

        // 2) Per-map (optional)
        String dir = parentDir(mapJsonPath);
        if (dir != null) {
            String overridePath = dir + "/spritedefs.json";
            try (InputStream in = resource(overridePath)) {
                if (in != null) {
                    JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in, StandardCharsets.UTF_8)));
                    putAll(merged, root); // overrides by key
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed loading per-map spritedefs: " + overridePath, e);
            }
        }

        return new SpriteDefsLoader(merged);
    }

    /** Get the sprite definition for an enemy type (case-insensitive). */
    public SpriteDef get(String enemyType) {
        SpriteDef d = byType.get(enemyType.toLowerCase());
        if (d == null) {
            throw new IllegalArgumentException("No spritedef for enemy type: " + enemyType);
        }
        return d;
    }

    // ---- helpers ----

    private static void putAll(Map<String, SpriteDef> out, JSONObject root) {
        for (String key : root.keySet()) {
            JSONObject def = root.getJSONObject(key);

            String front = def.getString("front");
            String side  = def.getString("side");
            String back  = def.getString("back");

            float w = 0.9f, h = 1.8f;
            if (def.has("scale")) {
                JSONObject s = def.getJSONObject("scale");
                w = (float) s.optDouble("width",  0.9);
                h = (float) s.optDouble("height", 1.8);
            }

            out.put(key.toLowerCase(), new SpriteDef(front, side, back, w, h));
        }
    }

    private static InputStream resource(String path) {
        // Try classpath first
        InputStream in = SpriteDefsLoader.class.getResourceAsStream(path);
        if (in != null) return in;
        // Fallback to filesystem
        try {
            return new java.io.FileInputStream(path);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String parentDir(String path) {
        if (path == null) return null;
        int s1 = path.lastIndexOf('/');
        int s2 = path.lastIndexOf('\\');
        int idx = Math.max(s1, s2);
        return (idx >= 0) ? path.substring(0, idx) : null;
    }
}