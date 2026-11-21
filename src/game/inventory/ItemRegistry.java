package game.inventory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemRegistry {

    private static final Map<String, ItemDefinition> ITEMS = new LinkedHashMap<>();

    private ItemRegistry() {
    }

    public static void reset() {
        ITEMS.clear();
    }

    public static void override(String id, ItemDefinition def) {
        if (def == null) throw new IllegalArgumentException("definition must not be null");
        ITEMS.put(id, def);
    }

    public static void register(ItemDefinition def) {
        if (def == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        String id = def.getId();
        if (ITEMS.containsKey(id)) {
            throw new IllegalArgumentException("Item id already registered: " + id);
        }
        ITEMS.put(id, def);
    }

    public static ItemDefinition get(String id) {
        return ITEMS.get(id);
    }

    public static Map<String, ItemDefinition> all() {
        return Collections.unmodifiableMap(ITEMS);
    }
}
