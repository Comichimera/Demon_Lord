package game.ai.conditions;

import game.ai.AIAgent;
import game.ai.AICondition;
import game.ai.Blackboard;
import org.json.JSONObject;

public final class SuspicionBelowCondition implements AICondition {
    private final float threshold;

    public static AICondition fromJson(JSONObject p) {
        return new SuspicionBelowCondition((float) p.optDouble("threshold", 0.5));
    }

    public SuspicionBelowCondition(float threshold) { this.threshold = threshold; }

    @Override public boolean test(AIAgent agent, Blackboard bb) {
        return bb.suspicion <= threshold;
    }
}
