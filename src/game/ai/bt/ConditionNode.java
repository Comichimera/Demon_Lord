package game.ai.bt;

import game.ai.AIAgent;
import game.ai.AICondition;
import game.ai.Action;
import game.ai.Blackboard;

public final class ConditionNode implements Node {
    private final AICondition cond;
    public ConditionNode(AICondition cond) { this.cond = cond; }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        return cond.test(agent, bb) ? Action.Status.SUCCESS : Action.Status.FAILURE;
    }
}
