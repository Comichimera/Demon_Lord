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

            TileDefinition base;
            try {
                base = TileRegistry.get(id);
            } catch (RuntimeException e) {
                base = null;
            }

            if (base != null) {
                String name       = patch.has("name")      ? patch.getString("name")      : base.getName();
                boolean walkable  = patch.has("walkable")  ? patch.getBoolean("walkable") : base.isWalkable();
                boolean openable  = patch.has("openable")  ? patch.getBoolean("openable") : base.isOpenable();
                boolean endsLevel = patch.has("endsLevel") ? patch.getBoolean("endsLevel"): base.endsLevel();
                String renderer   = patch.has("renderer")  ? patch.getString("renderer")  : base.getRenderer();
                String texture    = patch.has("texture")   ? patch.getString("texture")   : base.getTexture();
                String category   = patch.has("category")  ? patch.getString("category")  : base.getCategory();

                TileRegistry.override(id, new TileDefinition(
                        id, name, walkable, openable, endsLevel, renderer, texture, category
                ));
            } else {
                if (!patch.has("renderer") || !patch.has("texture")) {
                    throw new IllegalArgumentException(
                            "New tile '" + id + "' is missing renderer or texture"
                    );
                }
                String name       = patch.has("name")      ? patch.getString("name")      : ("Tile " + id);
                boolean walkable  = patch.has("walkable")  ? patch.getBoolean("walkable") : false;
                boolean openable  = patch.has("openable")  ? patch.getBoolean("openable") : false;
                boolean endsLevel = patch.has("endsLevel") ? patch.getBoolean("endsLevel"): false;
                String renderer   = patch.getString("renderer");
                String texture    = patch.getString("texture");
                String category   = patch.has("category")  ? patch.getString("category")  : "default";

                TileRegistry.override(id, new TileDefinition(
                        id, name, walkable, openable, endsLevel, renderer, texture, category
                ));
            }
        }
    }

    private static String inferDefaultCategory(char id) {
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
        if (s.endsWith("s")) s = s.substring(0, s.length()-1);
        return s.isEmpty() ? "tile" : s;
    }
}