package game.ai.bt;

import game.ai.AIAgent;
import game.ai.Action;
import game.ai.Blackboard;

public final class Wait implements Node {
    private final float seconds;
    private float remaining;
    private boolean active = false;

    public Wait(float seconds) { this.seconds = seconds; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        if (!active) { remaining = seconds; active = true; }
        remaining -= dt;
        if (remaining > 0) return Action.Status.RUNNING;
        active = false;
        return Action.Status.SUCCESS;
    }
}
