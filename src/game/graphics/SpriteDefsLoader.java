package game.graphics;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public final class SpriteDefsLoader {

    public static final class SpriteDef {
        public final String frontPath;
        public final String sidePath;
        public final String sideLeftPath;
        public final String sideRightPath;
        public final String backPath;
        public final float widthTiles;
        public final float heightTiles;

        SpriteDef(String frontPath, String sidePath, String sideL, String sideR, String backPath, float widthTiles, float heightTiles) {
            this.frontPath = frontPath;
            this.sidePath = sidePath;
            this.sideLeftPath = sideL != null ? sideL : sidePath;
            this.sideRightPath = sideR != null ? sideR : sidePath;
            this.backPath = backPath;
            this.widthTiles = widthTiles;
            this.heightTiles = heightTiles;
        }
    }

    private final Map<String, SpriteDef> byType = new HashMap<>();

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

    private SpriteDefsLoader(Map<String, SpriteDef> prebuilt) {
        this.byType.putAll(prebuilt);
    }
    public static SpriteDefsLoader loadForMap(String mapJsonPath) {
        Map<String, SpriteDef> merged = new HashMap<>();

        try (InputStream in = resource("/data/map/spritedefs.json")) {
            if (in != null) {
                JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in, StandardCharsets.UTF_8)));
                putAll(merged, root);
            }
        } catch (Exception ignore) {
        }

        String dir = parentDir(mapJsonPath);
        if (dir != null) {
            String overridePath = dir + "/spritedefs.json";
            try (InputStream in = resource(overridePath)) {
                if (in != null) {
                    JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in, StandardCharsets.UTF_8)));
                    putAll(merged, root);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed loading per-map spritedefs: " + overridePath, e);
            }
        }

        return new SpriteDefsLoader(merged);
    }

    public SpriteDef get(String enemyType) {
        SpriteDef d = byType.get(enemyType.toLowerCase());
        if (d == null) {
            throw new IllegalArgumentException("No spritedef for enemy type: " + enemyType);
        }
        return d;
    }

    private static void putAll(Map<String, SpriteDef> out, JSONObject root) {
        for (String key : root.keySet()) {
            JSONObject def = root.getJSONObject(key);
            String front = def.getString("front");
            String side  = def.getString("side");
            String back  = def.getString("back");
            String sideL = def.optString("side_left",  null);
            String sideR = def.optString("side_right", null);
            JSONObject scale = def.getJSONObject("scale");
            float w = (float) scale.getDouble("width");
            float h = (float) scale.getDouble("height");
            out.put(key.toLowerCase(), new SpriteDef(front, side, sideL, sideR, back, w, h));
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