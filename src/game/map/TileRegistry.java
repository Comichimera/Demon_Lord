package game.map;

import java.util.HashMap;
import java.util.Map;

public class TileRegistry {
    private static Map<Character, TileDefinition> baseDefs;
    private static Map<Character,TileDefinition> defs;

    public static java.util.Collection<TileDefinition> all() { return defs.values(); }

    public static void initialize(Map<Character,TileDefinition> baseline) {
        baseDefs = Map.copyOf(baseline);
        reset();
    }

    // Retrieve the definition for this tile‐ID
    public static TileDefinition get(char id) {
        TileDefinition td = defs.get(id);
        if (td == null) {
            throw new RuntimeException("No TileDefinition for ID: " + id);
        }
        return td;
    }

    public static java.util.Set<Character> idsForCategory(String category) {
        String norm = category == null ? "" : category.trim().toLowerCase();
        if (norm.endsWith("s")) norm = norm.substring(0, norm.length()-1);
        java.util.Set<Character> ids = new java.util.HashSet<>();
        for (TileDefinition td : defs.values()) {
            String c = td.getCategory();
            if (c != null) {
                String cn = c.toLowerCase();
                if (cn.endsWith("s")) cn = cn.substring(0, cn.length()-1);
                if (cn.equals(norm)) ids.add(td.getId());
            }
        }
        return ids;
    }

    public static void reset() {
        defs = new HashMap<>(baseDefs);
    }

    public static void override(char id, TileDefinition def) {
        defs.put(id, def);
    }
}
