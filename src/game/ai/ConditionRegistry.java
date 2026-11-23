package game.ai;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ConditionRegistry {
    public interface Factory extends Function<JSONObject, IAICondition> {}

    private static final Map<String, Factory> map = new HashMap<>();

    public static void register(String name, Factory f) { map.put(name, f); }
    public static IAICondition create(String name, JSONObject params) {
        Factory f = map.get(name);
        if (f == null) throw new IllegalArgumentException("Unknown condition: " + name);
        return f.apply(params == null ? new JSONObject() : params);
    }
}
