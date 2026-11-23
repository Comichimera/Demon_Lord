package game.map;

import game.config.GameConfig;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public final class SpriteDefsLoader {

    public static final class AnimDef {
        public final String sheetPath;
        public final int cols;
        public final int rows;
        public final int start;
        public final int count;
        public final float fps;
        public final int[] frameMs;

        AnimDef(String sheetPath, int cols, int rows, int start, int count, float fps, int[] frameMs) {
            this.sheetPath = sheetPath;
            this.cols = cols;
            this.rows = rows;
            this.start = start;
            this.count = count;
            this.fps = fps;
            this.frameMs = frameMs;
        }
    }

    public static final class SpriteDef {
        public final AnimDef front;
        public final AnimDef side;
        public final AnimDef sideLeft;
        public final AnimDef sideRight;
        public final AnimDef back;

        public final float widthTiles;
        public final float heightTiles;

        public final float hoverTiles;
        public final float bobAmpTiles;
        public final float bobHz;

        SpriteDef(AnimDef front, AnimDef side, AnimDef sideL, AnimDef sideR, AnimDef back,
                  float widthTiles, float heightTiles,
                  float hoverTiles, float bobAmpTiles, float bobHz) {
            this.front = front;
            this.side = side;
            this.sideLeft = (sideL != null) ? sideL : side;
            this.sideRight = (sideR != null) ? sideR : side;
            this.back = back;
            this.widthTiles = widthTiles;
            this.heightTiles = heightTiles;
            this.hoverTiles = hoverTiles;
            this.bobAmpTiles = bobAmpTiles;
            this.bobHz = bobHz;
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

        try (InputStream in = resource(GameConfig.path("sprites","/data/map/spritedefs.json"))) {
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

    private static AnimDef parseAnim(Object v) {
        if (v instanceof String s) {
            return new AnimDef(s, 1, 1, 0, 1, 1f, null);
        }

        JSONObject o = (JSONObject) v;

        String sheet = o.getString("sheet");
        int cols = o.getInt("cols");
        int rows = o.getInt("rows");

        int start = o.optInt("start", 0);
        int count = o.optInt("count", cols * rows);
        float fps = (float) o.optDouble("fps", 8.0);

        int[] frameMs = null;
        if (o.has("frame_ms")) {
            var arr = o.getJSONArray("frame_ms");
            frameMs = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++) frameMs[i] = arr.getInt(i);
        }

        return new AnimDef(sheet, cols, rows, start, count, fps, frameMs);
    }

    private static void putAll(Map<String, SpriteDef> out, JSONObject root) {
        for (String key : root.keySet()) {
            JSONObject def = root.getJSONObject(key);

            AnimDef front = parseAnim(def.get("front"));
            AnimDef side  = parseAnim(def.get("side"));
            AnimDef back  = parseAnim(def.get("back"));

            AnimDef sideL = def.has("side_left")  ? parseAnim(def.get("side_left"))  : null;
            AnimDef sideR = def.has("side_right") ? parseAnim(def.get("side_right")) : null;

            JSONObject scale = def.getJSONObject("scale");
            float w = (float) scale.getDouble("width");
            float h = (float) scale.getDouble("height");

            float hoverTiles = (float) def.optDouble("hover_tiles", 0.0);
            float bobAmpTiles = 0f;
            float bobHz = 0f;
            if (def.has("bob")) {
                JSONObject bob = def.getJSONObject("bob");
                bobAmpTiles = (float) bob.optDouble("amp_tiles", 0.0);
                bobHz = (float) bob.optDouble("hz", 0.0);
            }

            out.put(key.toLowerCase(),
                    new SpriteDef(front, side, sideL, sideR, back, w, h, hoverTiles, bobAmpTiles, bobHz));
        }
    }

    private static InputStream resource(String path) {
        InputStream in = SpriteDefsLoader.class.getResourceAsStream(path);
        if (in != null) return in;
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