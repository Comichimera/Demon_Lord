package game.ai;

public interface AICondition {
    boolean test(AIAgent agent, Blackboard bb);
}