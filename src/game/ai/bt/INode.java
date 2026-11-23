package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

public interface INode {
    IAction.Status tick(float dt, IAIAgent agent, Blackboard bb);
    default void onEnter(IAIAgent agent, Blackboard bb) {}
    default void onExit(IAIAgent agent, Blackboard bb) {}
}