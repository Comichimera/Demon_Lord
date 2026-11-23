package game.ai.conditions;

import game.ai.IAIAgent;
import game.ai.IAICondition;
import game.ai.Blackboard;
import org.json.JSONObject;

public final class PlayerVisibleCondition implements IAICondition {
    private final float maxSecondsSinceSeen;

    public static IAICondition fromJson(JSONObject p) {
        return new PlayerVisibleCondition((float)p.optDouble("recentSeconds", 0.2));
    }

    public PlayerVisibleCondition(float recent) { this.maxSecondsSinceSeen = recent; }

    @Override public boolean test(IAIAgent agent, Blackboard bb) {
        return bb.lastSeenPlayerCell.isPresent() && bb.timeSinceSeenPlayer <= maxSecondsSinceSeen;
    }
}
