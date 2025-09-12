package game.ai.bt;

import game.ai.*;

public final class ActionNode implements Node {
    private final Action action;
    private boolean entered = false;

    public ActionNode(Action action) { this.action = action; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        if (!entered) { action.enter(agent, bb); entered = true; }
        Action.Status s = action.tick(dt, agent, bb);
        if (s != Action.Status.RUNNING) { action.exit(agent, bb); entered = false; }
        return s;
    }
}
