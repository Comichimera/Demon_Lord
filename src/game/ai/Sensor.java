package game.ai;

public interface Sensor {
    void sample(float dt, AIAgent agent, Blackboard bb);
}
