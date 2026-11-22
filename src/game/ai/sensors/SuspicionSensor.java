package game.ai.sensors;

import game.ai.AIAgent;
import game.ai.Blackboard;
import game.ai.Sensor;

public final class SuspicionSensor implements Sensor {
    private static final float RISE_SIGHT  = 1.5f;
    private static final float RISE_SOUND  = 0.3f;
    private static final float DECAY       = 0.5f;
    private static final float SOUND_WINDOW = 3.0f;

    @Override public void sample(float dt, AIAgent agent, Blackboard bb) {
        if (bb.timeSinceSeenPlayer == 0f) {
            bb.suspicion = Math.min(1f, bb.suspicion + RISE_SIGHT * dt);
        } else if (bb.timeSinceHeardNoise < SOUND_WINDOW) {
            bb.suspicion = Math.min(1f, bb.suspicion + RISE_SOUND * dt);
        } else {
            bb.suspicion = Math.max(0f, bb.suspicion - DECAY * dt);
        }
    }
}
