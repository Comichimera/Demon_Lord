package game.ai.conditions;

import game.ai.AIAgent;
import game.ai.AICondition;
import game.ai.Blackboard;
import org.joml.Vector2i;
import org.json.JSONObject;

public final class HeardNoiseWithinCondition implements AICondition {
    private final int radius;
    private final float recentSeconds;

    public static AICondition fromJson(JSONObject p) {
        return new HeardNoiseWithinCondition(p.optInt("radius", 5), (float)p.optDouble("recentSeconds", 2.0));
    }

    public HeardNoiseWithinCondition(int radius, float recentSeconds) {
        this.radius = radius; this.recentSeconds = recentSeconds;
    }

    @Override public boolean test(AIAgent agent, Blackboard bb) {
        if (bb.lastHeardNoiseCell.isEmpty()) return false;
        if (bb.timeSinceHeardNoise > recentSeconds) return false;
        Vector2i me = agent.getCell(); Vector2i n = bb.lastHeardNoiseCell.get();
        return Math.abs(me.x - n.x) + Math.abs(me.y - n.y) <= radius;
    }
}
