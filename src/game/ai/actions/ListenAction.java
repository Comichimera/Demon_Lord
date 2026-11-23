package game.ai.actions;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;
import org.json.JSONObject;

public final class ListenAction implements IAction {
    private float remaining;

    public static IAction fromJson(JSONObject p) {
        return new ListenAction((float)p.optDouble("duration", 0.8));
    }

    public ListenAction(float duration) { this.remaining = duration; }

    @Override public Status tick(float dt, IAIAgent agent, Blackboard bb) {
        remaining -= dt;
        return remaining > 0 ? Status.RUNNING : Status.SUCCESS;
    }
}
