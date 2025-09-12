package game.ai.bt;

import game.ai.*;

public final class Wait implements Node {
    private float remaining;
    public Wait(float seconds) { this.remaining = seconds; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        remaining -= dt;
        return remaining > 0 ? Action.Status.RUNNING : Action.Status.SUCCESS;
    }
}
