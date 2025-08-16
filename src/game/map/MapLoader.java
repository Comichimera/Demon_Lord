package game.map;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import game.map.TileRegistry;
import game.map.TileDefinition;
import game.map.TileDefinitionLoader;

public class MapLoader {
    public static MapData loadMap(String path) {
        TileRegistry.reset();
        TileDefinitionLoader.loadAndMergeMapDefinitions(path);

        try (InputStream is = MapLoader.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Map file not found: " + path);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());

            // Build raw Tile grid from layout
            JSONArray layoutArr = json.getJSONArray("layout");
            int rows = layoutArr.length();
            int cols = layoutArr.getJSONArray(0).length();
            Tile[][] tiles = new Tile[rows][cols];
            for (int y = 0; y < rows; y++) {
                JSONArray row = layoutArr.getJSONArray(y);
                for (int x = 0; x < cols; x++) {
                    char id = row.getString(x).charAt(0);
                    TileDefinition td = TileRegistry.get(id);
                    tiles[y][x] = new Tile(td);
                }
            }

            // Apply per-type overrides
            if (json.has("per-type")) {
                JSONObject typeSection = json.getJSONObject("per-type").getJSONObject("overrides");
                for (String typeKey : typeSection.keySet()) {
                    JSONObject overrideJson = typeSection.getJSONObject(typeKey);
                    TileFlags flags = TileFlags.fromJson(overrideJson);

                    java.util.Set<Character> targets = TileRegistry.idsForCategory(typeKey);
                    if (targets.isEmpty()) continue;

                    for (int y = 0; y < rows; y++) {
                        for (int x = 0; x < cols; x++) {
                            if (targets.contains(tiles[y][x].getType())) {
                                flags.applyTo(tiles[y][x]);
                            }
                        }
                    }
                }
            }

            // Apply per-tile overrides
            if (json.has("per-tile")) {
                JSONArray perTileArr = json.getJSONObject("per-tile").getJSONArray("overrides");
                for (int i = 0; i < perTileArr.length(); i++) {
                    JSONObject ovr = perTileArr.getJSONObject(i);
                    int x = ovr.getInt("x");
                    int y = ovr.getInt("y");
                    TileFlags flags = TileFlags.fromJson(ovr);
                    flags.applyTo(tiles[y][x]);
                }
            }

            // Parse player spawn & camera yaw
            JSONObject playerObj = json.getJSONObject("player");
            int spawnX = playerObj.getJSONObject("spawn").getInt("x");
            int spawnY = playerObj.getJSONObject("spawn").getInt("y");
            float yaw  = playerObj.getJSONObject("camera").getFloat("yaw");

            java.util.List<org.json.JSONObject> objectiveSpecs = new java.util.ArrayList<>();
            if (json.has("objectives")) {
                org.json.JSONArray arr = json.getJSONArray("objectives");
                for (int i = 0; i < arr.length(); i++) {
                    objectiveSpecs.add(arr.getJSONObject(i));
                }
            }

            // Return the assembled MapData
            return new MapData(tiles, spawnX, spawnY, yaw, objectiveSpecs);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map", e);
        }
    }
}

