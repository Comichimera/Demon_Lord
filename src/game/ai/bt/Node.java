package game.ai.bt;

import game.ai.*;

public interface Node {
    Action.Status tick(float dt, AIAgent agent, Blackboard bb);
    default void onEnter(AIAgent agent, Blackboard bb) {}
    default void onExit(AIAgent agent, Blackboard bb) {}
}