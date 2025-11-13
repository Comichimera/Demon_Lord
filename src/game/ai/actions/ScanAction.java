// src/game/ai/actions/ScanAction.java
package game.ai.actions;

import game.ai.*;
import game.world.enemy.EnemyEntity;
import org.json.JSONObject;

public final class ScanAction implements Action {
    // parsed config
    private final float duration;
    private final boolean rotate;
    private final String mode;
    private final float degPerSec;
    private final float sweepDeg;
    private final boolean randomStart;

    // per-run state
    private float remaining;
    private float baseAngleDeg;
    private float elapsed;

    public static Action fromJson(JSONObject p) {
        float  duration     = (float) p.optDouble("duration", 0.5);
        boolean rotate      = p.optBoolean("rotate", false);
        String mode         = p.optString("mode", "spin");
        float  degPerSec    = (float) p.optDouble("deg_per_sec", 120.0);
        float  sweepDeg     = (float) p.optDouble("sweep_deg", 120.0);
        boolean randomStart = p.optBoolean("random_start", true);
        return new ScanAction(duration, rotate, mode, degPerSec, sweepDeg, randomStart);
    }

    public ScanAction(float duration, boolean rotate, String mode,
                      float degPerSec, float sweepDeg, boolean randomStart) {
        this.duration     = duration;
        this.rotate       = rotate;
        this.mode         = (mode == null ? "spin" : mode.toLowerCase());
        this.degPerSec    = degPerSec;
        this.sweepDeg     = sweepDeg;
        this.randomStart  = randomStart;
    }

    @Override public void enter(AIAgent agent, Blackboard bb) {
        remaining = duration;
        elapsed   = 0f;

        EnemyEntity self = (EnemyEntity) agent;
        baseAngleDeg = self.getYaw();

        if (rotate && randomStart) {
            baseAngleDeg = agent.rng().nextFloat() * 360f;
        }
    }

    @Override public Status tick(float dt, AIAgent agent, Blackboard bb) {
        if (rotate) {
            elapsed += dt;

            float angleDeg;
            if ("oscillate".equals(mode)) {
                float half = Math.max(0f, sweepDeg) * 0.5f;
                float phase = (float) Math.toRadians(elapsed * degPerSec);
                angleDeg = baseAngleDeg + (float) Math.sin(phase) * half;
            } else { // "spin"
                angleDeg = baseAngleDeg + degPerSec * elapsed;
            }

            EnemyEntity self = (EnemyEntity) agent;
            float rad = (float) Math.toRadians(angleDeg);
            float tx = self.getX() + (float) Math.cos(rad);
            float tz = self.getZ() + (float) Math.sin(rad);
            self.faceToward(tx, tz);
        }

        remaining -= dt;
        return remaining > 0f ? Status.RUNNING : Status.SUCCESS;
    }

    @Override public void exit(AIAgent agent, Blackboard bb) {
        if (rotate && !"oscillate".equals(mode)) {
            baseAngleDeg = (baseAngleDeg + degPerSec * elapsed) % 360f;
        }
    }
}
