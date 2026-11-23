package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

import java.util.List;

public final class Sequence implements INode {
    private final List<INode> children;
    private int index = 0;

    public Sequence(List<INode> children) { this.children = children; }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        while (index < children.size()) {
            INode child = children.get(index);
            IAction.Status s = child.tick(dt, agent, bb);
            if (s == IAction.Status.RUNNING) return s;
            if (s == IAction.Status.FAILURE) { index = 0; return s; }
            index++;
        }
        index = 0;
        return IAction.Status.SUCCESS;
    }
}
