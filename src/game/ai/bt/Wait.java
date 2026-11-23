package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

public final class Wait implements INode {
    private final float seconds;
    private float remaining;
    private boolean active = false;

    public Wait(float seconds) { this.seconds = seconds; }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        if (!active) { remaining = seconds; active = true; }
        remaining -= dt;
        if (remaining > 0) return IAction.Status.RUNNING;
        active = false;
        return IAction.Status.SUCCESS;
    }
}
