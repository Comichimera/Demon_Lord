package game.map;

import java.util.Map;

public class TileRegistry {
    private static Map<Character,TileDefinition> defs;

    /** Call once at startup */
    public static void initialize(Map<Character,TileDefinition> definitions) {
        defs = definitions;
    }

    /** Retrieve the definition for this tile‐ID */
    public static TileDefinition get(char id) {
        TileDefinition td = defs.get(id);
        if (td == null) {
            throw new RuntimeException("No TileDefinition for ID: " + id);
        }
        return td;
    }

    public static void override(char id, TileDefinition def) {
        defs.put(id, def);
    }
}
