package game.ai;

import game.ai.bt.INode;
import game.ai.io.BehaviorTreeLoader;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public final class BehaviorController {
    private final INode root;
    private final List<ISensor> sensors;
    private final Blackboard bb = new Blackboard();
    private float sensorAccumulator = 0f;
    private final float sensorHz;

    private BehaviorController(INode root, List<ISensor> sensors, float sensorHz) {
        this.root = root; this.sensors = sensors; this.sensorHz = sensorHz;
    }

    public static BehaviorController fromJsonResource(InputStream in, List<ISensor> sensors, float sensorHz) {
        try {
            INode n = BehaviorTreeLoader.loadFromStream(Objects.requireNonNull(in));
            return new BehaviorController(n, sensors, sensorHz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load behavior", e);
        }
    }

    public void tick(float dt, IAIAgent agent) {
        sensorAccumulator += dt;
        float step = 1f / sensorHz;
        while (sensorAccumulator >= step) {
            for (ISensor s : sensors) s.sample(step, agent, bb);
            sensorAccumulator -= step;
        }
        root.tick(dt, agent, bb);
    }

    public Blackboard bb() { return bb; }
}
