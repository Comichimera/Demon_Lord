package game.ai.sensors;

import game.ai.*;
import org.joml.Vector2i;

public final class HearingSensor implements Sensor {
    private final float decaySeconds;

    public HearingSensor(float decaySeconds) { this.decaySeconds = decaySeconds; }

    @Override public void sample(float dt, AIAgent agent, Blackboard bb) {
        Vector2i noise = agent.world().lastNoiseCell();
        if (noise != null) {
            bb.lastHeardNoiseCell = java.util.Optional.of(new Vector2i(noise));
            bb.timeSinceHeardNoise = 0f;
        } else {
            bb.timeSinceHeardNoise += dt;
            if (bb.timeSinceHeardNoise > decaySeconds) {
                bb.lastHeardNoiseCell = java.util.Optional.empty();
            }
        }
    }
}