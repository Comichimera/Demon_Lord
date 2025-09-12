package game.ai;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import game.ai.bt.Node;

public final class BehaviorController {
    private final Node root;
    private final List<Sensor> sensors;
    private final Blackboard bb = new Blackboard();
    private float sensorAccumulator = 0f;
    private final float sensorHz;

    private BehaviorController(Node root, List<Sensor> sensors, float sensorHz) {
        this.root = root; this.sensors = sensors; this.sensorHz = sensorHz;
    }

    public static BehaviorController fromJsonResource(InputStream in, List<Sensor> sensors, float sensorHz) {
        try {
            Node n = game.ai.io.BehaviorTreeLoader.loadFromStream(Objects.requireNonNull(in));
            return new BehaviorController(n, sensors, sensorHz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load behavior", e);
        }
    }

    public void tick(float dt, AIAgent agent) {
        sensorAccumulator += dt;
        float step = 1f / sensorHz;
        while (sensorAccumulator >= step) {
            for (Sensor s : sensors) s.sample(step, agent, bb);
            sensorAccumulator -= step;
        }
        root.tick(dt, agent, bb);
    }

    public Blackboard bb() { return bb; }
}
