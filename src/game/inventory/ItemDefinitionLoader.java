package game.inventory;

import game.config.GameConfig;
import game.inventory.effect.HealEffect;
import game.inventory.effect.TreatInjuryEffect;
import game.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDefinitionLoader {

    public static void loadGlobal() {
        loadFromPath(GameConfig.path("items", "/data/items/items.json"));
    }

    public static void loadAndMergeLevel(String mapPath) {
        int slash = mapPath.lastIndexOf('/');
        if (slash < 0) return;
        String levelItemsPath = mapPath.substring(0, slash) + "/items.json";

        InputStream in = ItemDefinitionLoader.class.getResourceAsStream(levelItemsPath);
        if (in == null) return;

        parseAndRegister(in, levelItemsPath, true);
    }

    private static void loadFromPath(String path) {
        InputStream in = ItemDefinitionLoader.class.getResourceAsStream(path);
        if (in == null) throw new RuntimeException("items.json not found: " + path);
        parseAndRegister(in, path, false);
    }

    private static void parseAndRegister(InputStream in, String sourcePath, boolean merge) {
        JSONArray arr = new JSONArray(new JSONTokener(new InputStreamReader(in)));
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.getString("id");

            if (merge && ItemRegistry.get(id) != null) {
                ItemDefinition base = ItemRegistry.get(id);
                String name        = obj.has("name")        ? obj.getString("name")        : base.getName();
                String description = obj.has("description") ? obj.getString("description") : base.getDescription();
                String icon        = obj.has("icon")        ? obj.getString("icon")        : base.getIconPath();
                boolean stackable  = obj.has("stackable")   ? obj.getBoolean("stackable")  : base.isStackable();
                int maxStack       = obj.has("maxStack")    ? obj.getInt("maxStack")       : base.getMaxStack();
                List<IItemEffect> effects = obj.has("effects") ? parseEffects(obj.getJSONArray("effects"), sourcePath) : base.getEffects();
                ItemRegistry.override(id, new ItemDefinition(id, name, description, icon, stackable, maxStack, new ArrayList<>(effects)));
            } else {
                String name        = obj.optString("name", id);
                String description = obj.optString("description", "");
                String icon        = obj.optString("icon", "");
                boolean stackable  = obj.optBoolean("stackable", false);
                int maxStack       = obj.optInt("maxStack", 1);
                List<IItemEffect> effects = obj.has("effects") ? parseEffects(obj.getJSONArray("effects"), sourcePath) : Collections.emptyList();
                ItemRegistry.register(new ItemDefinition(id, name, description, icon, stackable, maxStack, effects));
            }
        }
    }

    private static List<IItemEffect> parseEffects(JSONArray arr, String sourcePath) {
        List<IItemEffect> effects = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.getJSONObject(i);
            String type = e.getString("type");
            switch (type) {
                case "heal":
                    effects.add(new HealEffect((int) e.getFloat("amount")));
                    break;
                case "treat_injury":
                    effects.add(new TreatInjuryEffect(e.getFloat("amount")));
                    break;
                default:
                    Log.warn("Unknown item effect type '" + type + "' in " + sourcePath);
            }
        }
        return effects;
    }
}
