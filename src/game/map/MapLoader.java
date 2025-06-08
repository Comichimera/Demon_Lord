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
        TileDefinitionLoader.loadAndMergeMapDefinitions(path);

        try (InputStream is = MapLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Map file not found: " + path);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());

            // 1) Read textures
            JSONObject tex = json.getJSONObject("textures");
            String wallTex = tex.getString("wall");
            String exitTex = tex.getString("exit");
            String doorTex = tex.getString("door");

            // 2) Build raw Tile grid from layout
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

            // 3) Apply per-type overrides
            if (json.has("per-type")) {
                JSONObject typeSection = json
                        .getJSONObject("per-type")
                        .getJSONObject("overrides");
                for (String typeKey : typeSection.keySet()) {
                    JSONObject overrideJson = typeSection.getJSONObject(typeKey);
                    TileFlags flags = TileFlags.fromJson(overrideJson);

                    // Map your keys to the tile character:
                    //   “walls” → 'W', “doors” → 'D', “exits” → 'E', “floor”→'F'
                    char tileChar;
                    switch (typeKey.toLowerCase()) {
                        case "walls":  tileChar = 'W'; break;
                        case "doors":  tileChar = 'D'; break;
                        case "exits":  tileChar = 'E'; break;
                        case "floor":  tileChar = 'F'; break;
                        default:
                            continue;  // unknown key, skip
                    }

                    // Apply to every tile of that type
                    for (int y = 0; y < rows; y++) {
                        for (int x = 0; x < cols; x++) {
                            if (tiles[y][x].getType() == tileChar) {
                                flags.applyTo(tiles[y][x]);
                            }
                        }
                    }
                }
            }

            // 4) Apply per-tile overrides
            if (json.has("per-tile")) {
                JSONArray perTileArr = json
                        .getJSONObject("per-tile")
                        .getJSONArray("overrides");
                for (int i = 0; i < perTileArr.length(); i++) {
                    JSONObject ovr = perTileArr.getJSONObject(i);
                    int x = ovr.getInt("x");
                    int y = ovr.getInt("y");
                    TileFlags flags = TileFlags.fromJson(ovr);
                    // Coordinates assumed valid; you could clamp/check here
                    flags.applyTo(tiles[y][x]);
                }
            }

            // 5) Parse player spawn & camera yaw
            JSONObject playerObj = json.getJSONObject("player");
            int spawnX = playerObj
                    .getJSONObject("spawn")
                    .getInt("x");
            int spawnY = playerObj
                    .getJSONObject("spawn")
                    .getInt("y");
            float yaw = playerObj
                    .getJSONObject("camera")
                    .getFloat("yaw");

            // 6) Return the assembled MapData
            return new MapData(
                    tiles,
                    spawnX, spawnY,
                    wallTex, exitTex,
                    yaw,
                    doorTex
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map", e);
        }
    }
}
