package game.ai.actions;

import game.ai.*;
import org.json.JSONObject;

public final class ListenAction implements Action {
    private float remaining;

    public static Action fromJson(JSONObject p) {
        return new ListenAction((float)p.optDouble("duration", 0.8));
    }

    public ListenAction(float duration) { this.remaining = duration; }

    @Override public Status tick(float dt, AIAgent agent, Blackboard bb) {
        remaining -= dt;
        return remaining > 0 ? Status.RUNNING : Status.SUCCESS;
    }
}
