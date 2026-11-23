package game.ai;

public interface IAICondition {
    boolean test(IAIAgent agent, Blackboard bb);
}