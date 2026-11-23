package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

import java.util.List;

public final class Selector implements INode {
    private final List<INode> children;

    public Selector(List<INode> children) { this.children = children; }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        for (INode child : children) {
            IAction.Status s = child.tick(dt, agent, bb);
            if (s != IAction.Status.FAILURE) return s;
        }
        return IAction.Status.FAILURE;
    }
}