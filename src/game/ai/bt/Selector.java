package game.ai.bt;

import java.util.List;
import game.ai.*;

public final class Selector implements Node {
    private final List<Node> children;
    private int runningIndex = -1;

    public Selector(List<Node> children) { this.children = children; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        int start = runningIndex >= 0 ? runningIndex : 0;
        for (int i = start; i < children.size(); i++) {
            Node child = children.get(i);
            Action.Status s = child.tick(dt, agent, bb);
            if (s == Action.Status.RUNNING) { runningIndex = i; return s; }
            if (s == Action.Status.SUCCESS) { runningIndex = -1; return s; }
            // else FAILURE → try next
        }
        runningIndex = -1;
        return Action.Status.FAILURE;
    }
}