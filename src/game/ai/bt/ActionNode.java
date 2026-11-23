package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

public final class ActionNode implements INode {
    private final IAction action;
    private boolean entered = false;

    public ActionNode(IAction action) { this.action = action; }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        if (!entered) { action.enter(agent, bb); entered = true; }
        IAction.Status s = action.tick(dt, agent, bb);
        if (s != IAction.Status.RUNNING) { action.exit(agent, bb); entered = false; }
        return s;
    }

    @Override public void onExit(IAIAgent agent, Blackboard bb) {
        if (entered) { action.exit(agent, bb); entered = false; }
    }
}
