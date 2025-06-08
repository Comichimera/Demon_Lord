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

        // Load the JSON from resources
        InputStream in = TileDefinitionLoader.class
                .getResourceAsStream("/data/map/tiledefs.json");
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

            TileDefinition td = new TileDefinition(
                    id, name, walkable, openable, endsLevel, renderer, texture
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

        InputStream in = TileDefinitionLoader.class
                .getResourceAsStream(overridePath);
        if (in == null) return;  // no per-map defs

        JSONObject root = new JSONObject(new JSONTokener(new InputStreamReader(in)));
        for (String key : root.keySet()) {
            if (key.length() != 1) continue;
            char id = key.charAt(0);
            JSONObject obj = root.getJSONObject(key);

            // parse exactly as in loadDefinitions()
            String name       = obj.getString("name");
            boolean walkable  = obj.getBoolean("walkable");
            boolean openable  = obj.getBoolean("openable");
            boolean endsLevel = obj.getBoolean("endsLevel");
            String renderer   = obj.getString("renderer");
            String texture    = obj.getString("texture");

            TileDefinition td = new TileDefinition(
                    id, name, walkable, openable, endsLevel, renderer, texture
            );
            TileRegistry.override(id, td);
        }
    }
}