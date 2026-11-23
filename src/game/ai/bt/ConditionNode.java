package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAICondition;
import game.ai.IAction;
import game.ai.Blackboard;

public final class ConditionNode implements INode {
    private final IAICondition cond;
    public ConditionNode(IAICondition cond) { this.cond = cond; }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        return cond.test(agent, bb) ? IAction.Status.SUCCESS : IAction.Status.FAILURE;
    }
}
