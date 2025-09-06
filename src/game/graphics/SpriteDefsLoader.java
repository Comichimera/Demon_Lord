package game.graphics;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class SpriteDefsLoader {

    public static final class SpriteDef {
        public final String frontPath;
        public final String sidePath;
        public final String backPath;
        public final float widthTiles;
        public final float heightTiles;

        SpriteDef(String f, String s, String b, float w, float h) {
            this.frontPath = f; this.sidePath = s; this.backPath = b;
            this.widthTiles = w; this.heightTiles = h;
        }
    }

    private final Map<String, SpriteDef> byType = new HashMap<>();

    // NOTE: removed TextureLoader from the constructor
    public SpriteDefsLoader(String resourcePath) {
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) throw new IllegalStateException("Missing spritedefs at " + resourcePath);
            JSONObject root = new JSONObject(new JSONTokener(in));
            for (String key : root.keySet()) {
                JSONObject def = root.getJSONObject(key);

                String pFront = def.getString("front");
                String pSide  = def.getString("side");
                String pBack  = def.getString("back");

                float w = 0.9f, h = 1.8f;
                if (def.has("scale")) {
                    JSONObject s = def.getJSONObject("scale");
                    w = (float) s.optDouble("width",  0.9);
                    h = (float) s.optDouble("height", 1.8);
                }

                byType.put(key.toLowerCase(), new SpriteDef(pFront, pSide, pBack, w, h));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed loading spritedefs: " + e.getMessage(), e);
        }
    }

    public SpriteDef get(String enemyType) {
        SpriteDef d = byType.get(enemyType.toLowerCase());
        if (d == null) throw new IllegalArgumentException("No spritedef for enemy type: " + enemyType);
        return d;
    }

    private static InputStream getResource(String path) {
        InputStream in = SpriteDefsLoader.class.getResourceAsStream(path);
        if (in != null) return in;
        try {
            return new java.io.FileInputStream(path);
        } catch (Exception ignore) { return null; }
    }
}
