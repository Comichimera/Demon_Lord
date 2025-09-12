package game.ai.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;
import game.ai.*;
import game.ai.bt.*;
import game.ai.actions.*;
import game.ai.conditions.*;

public final class BehaviorTreeLoader {

    public static Node loadFromStream(InputStream in) throws IOException {
        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(text).getJSONObject("root");
        bootstrapRegistries(); // register built-ins
        return build(root);
    }

    private static Node build(JSONObject node) {
        String type = node.getString("type");
        switch (type) {
            case "selector": return new Selector(children(node));
            case "sequence": return new Sequence(children(node));
            case "condition": {
                String name = node.getString("name");
                JSONObject params = node.optJSONObject("params");
                AICondition cond = ConditionRegistry.create(name, params);
                return new ConditionNode(cond);
            }
            case "action": {
                String name = node.getString("name");
                JSONObject params = node.optJSONObject("params");
                Action act = ActionRegistry.create(name, params);
                return new ActionNode(act);
            }
            case "wait": return new Wait((float)node.getDouble("seconds"));
            default: throw new IllegalArgumentException("Unknown node type: " + type);
        }
    }

    private static java.util.List<Node> children(JSONObject n) {
        JSONArray arr = n.getJSONArray("children");
        java.util.List<Node> list = new ArrayList<>(arr.length());
        for (int i=0;i<arr.length();i++) list.add(build(arr.getJSONObject(i)));
        return list;
    }

    private static void bootstrapRegistries() {
        // Actions
        ActionRegistry.register("look", LookAction::fromJson);
        ActionRegistry.register("scan", ScanAction::fromJson);
        ActionRegistry.register("listen", ListenAction::fromJson);
        ActionRegistry.register("move", MoveAction::fromJson);
        // Conditions
        ConditionRegistry.register("playerVisible", PlayerVisibleCondition::fromJson);
        ConditionRegistry.register("heardNoiseWithin", HeardNoiseWithinCondition::fromJson);
    }
}
