package game.map;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TileDefinitionLoader {

    public static Map<Character, TileDefinition> loadDefinitions() {
        Map<Character, TileDefinition> defs = new HashMap<>();

        InputStream in = TileDefinitionLoader.class.getResourceAsStream("/data/map/tiledefs.json");
        if (in == null) {
            throw new RuntimeException("tiledefs.json not found on classpath");
        }

        JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in)));
        for (String key : root.keySet()) {
            if (key.length() != 1) continue;  // skip invalid keys
            char id = key.charAt(0);
            JSONObject obj = root.getJSONObject(key);

            String name       = obj.getString("name");
            boolean walkable  = obj.getBoolean("walkable");
            boolean openable  = obj.getBoolean("openable");
            boolean endsLevel = obj.getBoolean("endsLevel");
            String renderer   = obj.getString("renderer");
            String texture    = obj.getString("texture");

            // NEW: category is optional in baseline JSON; infer a sensible default if missing
            String category   = normalizeCategory(obj.optString("category", inferDefaultCategory(id)));

            TileDefinition td = new TileDefinition(
                    id, name, walkable, openable, endsLevel, renderer, texture, category
            );
            defs.put(id, td);
        }

        return defs;
    }

    public static void loadAndMergeMapDefinitions(String mapJsonPath) {
        // compute the directory containing the map
        int slash = mapJsonPath.lastIndexOf('/');
        if (slash < 0) return;
        String dir = mapJsonPath.substring(0, slash);
        String overridePath = dir + "/tiledefs.json";

        InputStream in = TileDefinitionLoader.class.getResourceAsStream(overridePath);
        if (in == null) return;  // no per-map defs

        JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in)));
        for (String key : root.keySet()) {
            if (key.length() != 1) continue;

            char id = key.charAt(0);
            JSONObject patch = root.getJSONObject(key);

            // Try to find an existing baseline definition first
            TileDefinition base = TileRegistry.get(id);

            if (base != null) {
                // --- Merge: inherit fields from baseline unless explicitly overridden ---
                String name       = patch.has("name")       ? patch.getString("name")       : base.getName();
                boolean walkable  = patch.has("walkable")   ? patch.getBoolean("walkable")  : base.isWalkable();
                boolean openable  = patch.has("openable")   ? patch.getBoolean("openable")  : base.isOpenable();
                boolean endsLevel = patch.has("endsLevel")  ? patch.getBoolean("endsLevel") : base.isEndsLevel();
                String renderer   = patch.has("renderer")   ? patch.getString("renderer")   : base.getRenderer();
                String texture    = patch.has("texture")    ? patch.getString("texture")    : base.getTexture();
                String category   = patch.has("category")
                        ? normalizeCategory(patch.getString("category"))
                        : base.getCategory();

                TileDefinition merged = new TileDefinition(
                        id, name, walkable, openable, endsLevel, renderer, texture, category
                );
                TileRegistry.override(id, merged);

            } else {
                // require essential fields or fail with a helpful message
                String renderer = patch.optString("renderer", null);
                String texture  = patch.optString("texture",  null);
                if (renderer == null || texture == null) {
                    throw new IllegalArgumentException(
                            "Per-level tiledefs (" + overridePath + ") introduces new tile '" + id +
                                    "' but is missing required field(s): " +
                                    (renderer == null ? "renderer " : "") +
                                    (texture  == null ? "texture"   : "")
                    );
                }

                String name       = patch.optString("name", "Tile " + id);
                boolean walkable  = patch.optBoolean("walkable",  false);
                boolean openable  = patch.optBoolean("openable",  false);
                boolean endsLevel = patch.optBoolean("endsLevel", false);
                String category   = normalizeCategory(patch.optString("category", inferDefaultCategory(id)));

                TileDefinition td = new TileDefinition(
                        id, name, walkable, openable, endsLevel, renderer, texture, category
                );
                TileRegistry.override(id, td);
            }
        }
    }

    // Helpers

    private static String inferDefaultCategory(char id) {
        // Fallbacks so old baseline JSON without "category" still works nicely
        switch (Character.toUpperCase(id)) {
            case 'W': return "wall";
            case 'D': return "door";
            case 'E': return "exit";
            case 'F': return "floor";
            default:  return "tile";
        }
    }

    private static String normalizeCategory(String s) {
        if (s == null) return "tile";
        s = s.trim().toLowerCase();
        // optional: singularize simple plurals ("walls" -> "wall")
        if (s.endsWith("s")) s = s.substring(0, s.length()-1);
        return s.isEmpty() ? "tile" : s;
    }
}