package game.ai.io;

import game.ai.IAICondition;
import game.ai.IAction;
import game.ai.ActionRegistry;
import game.ai.ConditionRegistry;
import game.ai.actions.ListenAction;
import game.ai.actions.LookAction;
import game.ai.actions.MoveAction;
import game.ai.actions.ScanAction;
import game.ai.bt.*;
import game.ai.conditions.HeardNoiseWithinCondition;
import game.ai.conditions.PlayerVisibleCondition;
import game.ai.conditions.SuspicionAboveCondition;
import game.ai.conditions.SuspicionBelowCondition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class BehaviorTreeLoader {

    public static INode loadFromStream(InputStream in) throws IOException {
        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(text).getJSONObject("root");
        bootstrapRegistries();
        return build(root);
    }

    private static INode build(JSONObject node) {
        String type = node.getString("type");
        switch (type) {
            case "selector": return new Selector(children(node));
            case "sequence": return new Sequence(children(node));
            case "condition": {
                String name = node.getString("name");
                JSONObject params = node.optJSONObject("params");
                IAICondition cond = ConditionRegistry.create(name, params);
                return new ConditionNode(cond);
            }
            case "action": {
                String name = node.getString("name");
                JSONObject params = node.optJSONObject("params");
                IAction act = ActionRegistry.create(name, params);
                return new ActionNode(act);
            }
            case "wait": return new Wait((float)node.getDouble("seconds"));
            case "parallel": {
                String sp = node.optString("successPolicy", "requireOne");
                String fp = node.optString("failurePolicy", "requireOne");
                Parallel.Policy succ = "requireAll".equals(sp) ? Parallel.Policy.REQUIRE_ALL : Parallel.Policy.REQUIRE_ONE;
                Parallel.Policy fail = "requireAll".equals(fp) ? Parallel.Policy.REQUIRE_ALL : Parallel.Policy.REQUIRE_ONE;
                return new Parallel(children(node), succ, fail);
            }
            default: throw new IllegalArgumentException("Unknown node type: " + type);
        }
    }

    private static List<INode> children(JSONObject n) {
        JSONArray arr = n.getJSONArray("children");
        List<INode> list = new ArrayList<>(arr.length());
        for (int i=0;i<arr.length();i++) list.add(build(arr.getJSONObject(i)));
        return list;
    }

    private static void bootstrapRegistries() {
        ActionRegistry.register("look", LookAction::fromJson);
        ActionRegistry.register("scan", ScanAction::fromJson);
        ActionRegistry.register("listen", ListenAction::fromJson);
        ActionRegistry.register("move", MoveAction::fromJson);
        ConditionRegistry.register("playerVisible", PlayerVisibleCondition::fromJson);
        ConditionRegistry.register("heardNoiseWithin", HeardNoiseWithinCondition::fromJson);
        ConditionRegistry.register("suspicionAbove", SuspicionAboveCondition::fromJson);
        ConditionRegistry.register("suspicionBelow", SuspicionBelowCondition::fromJson);
    }
}
