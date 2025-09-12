package game.ai.sensors;

import game.ai.*;
import org.joml.Vector2i;

public final class VisionSensor implements Sensor {
    private final float fovDeg;
    private final float maxDist;

    public VisionSensor(float fovDeg, float maxTiles) {
        this.fovDeg = fovDeg; this.maxDist = maxTiles;
    }

    @Override public void sample(float dt, AIAgent agent, Blackboard bb) {
        // Minimal: use LOS only; FOV hook is here if you track facing angles.
        // Replace `playerCell()` with your actual player cell lookup.
        Vector2i me = agent.getCell();
        Vector2i player = agent.world().playerCell(); // <-- add this in your impl
        float dist = agent.world().heuristicCost(me, player);
        boolean canSee = dist <= maxDist && agent.world().hasLineOfSight(me, player);
        if (canSee) {
            bb.lastSeenPlayerCell = java.util.Optional.of(new Vector2i(player));
            bb.timeSinceSeenPlayer = 0f;
        } else {
            bb.timeSinceSeenPlayer += dt;
        }
    }
}
