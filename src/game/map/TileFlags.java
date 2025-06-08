// File: src/game/map/TileFlags.java
package game.map;

import org.json.JSONObject;

/**
 * Temporary container for any overrides parsed from JSON.
 * Only non‑null fields will be applied to a Tile.
 */
public class TileFlags {
    private Boolean walkable;
    private Boolean endsLevel;
    private Boolean openable;

    /** Parse { "walkable":bool, "endsLevel":bool, "openable":bool } */
    public static TileFlags fromJson(JSONObject obj) {
        TileFlags f = new TileFlags();
        if (obj.has("walkable"))  f.walkable  = obj.getBoolean("walkable");
        if (obj.has("endsLevel")) f.endsLevel = obj.getBoolean("endsLevel");
        if (obj.has("openable"))  f.openable  = obj.getBoolean("openable");
        return f;
    }

    /** Apply any non‑null flags onto the given Tile */
    public void applyTo(Tile t) {
        if (walkable  != null) t.setWalkable(walkable);
        if (endsLevel != null) t.setEndsLevel(endsLevel);
        if (openable  != null) t.setOpenable(openable);
    }
}
