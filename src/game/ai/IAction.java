package game.ai;

public interface IAction {
    enum Status { RUNNING, SUCCESS, FAILURE }
    default void enter(IAIAgent agent, Blackboard bb) {}
    Status tick(float dt, IAIAgent agent, Blackboard bb);
    default void exit(IAIAgent agent, Blackboard bb) {}
}