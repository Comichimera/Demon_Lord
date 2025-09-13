package game.ai.actions;

import game.ai.*;
import org.json.JSONObject;
import org.joml.Vector2i;

public final class LookAction implements Action {
    private enum Target { PLAYER, POINT }
    private final Target target;
    private final Vector2i point;
    private float remaining;

    public static Action fromJson(JSONObject p) {
        String t = p.optString("target", "player");
        float duration = (float)p.optDouble("duration", 0.5);
        if ("point".equalsIgnoreCase(t)) {
            JSONObject pt = p.getJSONObject("point");
            return new LookAction(Target.POINT, new Vector2i(pt.getInt("x"), pt.getInt("y")), duration);
        }
        return new LookAction(Target.PLAYER, null, duration);
    }

    private LookAction(Target t, Vector2i pt, float duration) { this.target = t; this.point = pt; this.remaining = duration; }

    @Override public void enter(AIAgent agent, Blackboard bb) { }

    @Override public Status tick(float dt, AIAgent agent, Blackboard bb) {
        Vector2i to = null;
        if (target == Target.PLAYER) {
            to = agent.world().playerCell();
            if (to == null) return Status.FAILURE;
        } else {
            to = point;
        }
        agent.aimAt(to);
        remaining -= dt;
        return (remaining > 0f) ? Status.RUNNING : Status.SUCCESS;
    }
}
