package game.ai.conditions;

import game.ai.IAIAgent;
import game.ai.IAICondition;
import game.ai.Blackboard;
import org.json.JSONObject;

public final class SuspicionBelowCondition implements IAICondition {
    private final float threshold;

    public static IAICondition fromJson(JSONObject p) {
        return new SuspicionBelowCondition((float) p.optDouble("threshold", 0.5));
    }

    public SuspicionBelowCondition(float threshold) { this.threshold = threshold; }

    @Override public boolean test(IAIAgent agent, Blackboard bb) {
        return bb.suspicion <= threshold;
    }
}
