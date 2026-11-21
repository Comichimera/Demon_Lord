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
        Vector2i me = agent.getCell();
        Vector2i player = agent.world().playerCell();
        if (me == null || player == null) {
            bb.timeSinceSeenPlayer += dt;
            return;
        }

        int dx = player.x - me.x;
        int dy = player.y - me.y;

        float distSq = (float)(dx * dx + dy * dy);
        float maxSq  = maxDist * maxDist;
        if (distSq > maxSq) {
            bb.timeSinceSeenPlayer += dt;
            return;
        }

        boolean inFov = (fovDeg >= 359.9f) || withinFov(agent.getYawDeg(), dx, dy, fovDeg);
        if (!inFov) {
            bb.timeSinceSeenPlayer += dt;
            return;
        }

        boolean canSee = agent.world().hasLineOfSight(me, player);
        if (canSee) {
            bb.lastSeenPlayerCell = java.util.Optional.of(new Vector2i(player));
            bb.timeSinceSeenPlayer = 0f;
        } else {
            bb.timeSinceSeenPlayer += dt;
        }
    }

    private static boolean withinFov(float yawDeg, int dx, int dy, float fovDeg) {
        if (dx == 0 && dy == 0) return true;

        double toAngle = Math.toDegrees(Math.atan2(-dy, dx)) + 90.0;
        float delta = angleDelta((float)toAngle, yawDeg);
        return Math.abs(delta) <= (fovDeg * 0.5f);
    }

    private static float angleDelta(float a, float b) {
        return ((a - b + 540f) % 360f) - 180f;
    }
}
