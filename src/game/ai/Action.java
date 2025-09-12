package game.ai;

public interface Action {
    enum Status { RUNNING, SUCCESS, FAILURE }
    default void enter(AIAgent agent, Blackboard bb) {}
    Status tick(float dt, AIAgent agent, Blackboard bb);
    default void exit(AIAgent agent, Blackboard bb) {}
}