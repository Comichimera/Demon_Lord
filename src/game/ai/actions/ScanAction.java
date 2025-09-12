package game.ai.actions;

import game.ai.*;
import org.json.JSONObject;

public final class ScanAction implements Action {
    private float remaining;

    public static Action fromJson(JSONObject p) {
        return new ScanAction((float)p.optDouble("duration", 0.5));
    }
    public ScanAction(float duration) { this.remaining = duration; }

    @Override public Status tick(float dt, AIAgent agent, Blackboard bb) {
        // You can oscillate facing here if you want; for now just wait.
        remaining -= dt;
        return remaining > 0 ? Status.RUNNING : Status.SUCCESS;
    }
}
