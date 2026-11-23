package game.ai;

public interface ISensor {
    void sample(float dt, IAIAgent agent, Blackboard bb);
}
