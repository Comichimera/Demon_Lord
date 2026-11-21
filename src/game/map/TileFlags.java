package game.map;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TileFlags {
    private Boolean      walkable;
    private Boolean      endsLevel;
    private Boolean      openable;
    private Boolean      locked;
    private List<String> keys;

    public static TileFlags fromJson(JSONObject obj) {
        TileFlags f = new TileFlags();
        if (obj.has("walkable"))  f.walkable  = obj.getBoolean("walkable");
        if (obj.has("endsLevel")) f.endsLevel = obj.getBoolean("endsLevel");
        if (obj.has("openable"))  f.openable  = obj.getBoolean("openable");
        if (obj.has("locked"))    f.locked    = obj.getBoolean("locked");
        if (obj.has("keys")) {
            JSONArray arr = obj.getJSONArray("keys");
            f.keys = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) f.keys.add(arr.getString(i));
        }
        return f;
    }

    public void applyTo(Tile t) {
        if (walkable  != null) t.setWalkable(walkable);
        if (endsLevel != null) t.setEndsLevel(endsLevel);
        if (openable  != null) t.setOpenable(openable);
        if (locked    != null) t.setLocked(locked);
        if (keys      != null) t.setLockedByKeys(keys);
    }
}
