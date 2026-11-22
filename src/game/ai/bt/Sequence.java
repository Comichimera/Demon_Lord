package game.ai.bt;

import game.ai.AIAgent;
import game.ai.Action;
import game.ai.Blackboard;

import java.util.List;

public final class Sequence implements Node {
    private final List<Node> children;
    private int index = 0;

    public Sequence(List<Node> children) { this.children = children; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        while (index < children.size()) {
            Node child = children.get(index);
            Action.Status s = child.tick(dt, agent, bb);
            if (s == Action.Status.RUNNING) return s;
            if (s == Action.Status.FAILURE) { index = 0; return s; }
            index++;
        }
        index = 0;
        return Action.Status.SUCCESS;
    }
}
