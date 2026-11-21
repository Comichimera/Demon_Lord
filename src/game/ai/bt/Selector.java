package game.ai.bt;

import java.util.List;
import game.ai.*;

public final class Selector implements Node {
    private final List<Node> children;

    public Selector(List<Node> children) { this.children = children; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        for (Node child : children) {
            Action.Status s = child.tick(dt, agent, bb);
            if (s != Action.Status.FAILURE) return s;
        }
        return Action.Status.FAILURE;
    }
}