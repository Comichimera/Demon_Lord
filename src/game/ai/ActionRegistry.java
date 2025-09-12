package game.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.json.JSONObject;

public final class ActionRegistry {
    public interface Factory extends Function<JSONObject, Action> {}

    private static final Map<String, Factory> map = new HashMap<>();

    public static void register(String name, Factory f) { map.put(name, f); }
    public static Action create(String name, JSONObject params) {
        Factory f = map.get(name);
        if (f == null) throw new IllegalArgumentException("Unknown action: " + name);
        return f.apply(params == null ? new JSONObject() : params);
    }
}
