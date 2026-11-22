package game.ai.conditions;

import game.ai.AIAgent;
import game.ai.AICondition;
import game.ai.Blackboard;
import org.json.JSONObject;

public final class PlayerVisibleCondition implements AICondition {
    private final float maxSecondsSinceSeen;

    public static AICondition fromJson(JSONObject p) {
        return new PlayerVisibleCondition((float)p.optDouble("recentSeconds", 0.2));
    }

    public PlayerVisibleCondition(float recent) { this.maxSecondsSinceSeen = recent; }

    @Override public boolean test(AIAgent agent, Blackboard bb) {
        return bb.lastSeenPlayerCell.isPresent() && bb.timeSinceSeenPlayer <= maxSecondsSinceSeen;
    }
}
