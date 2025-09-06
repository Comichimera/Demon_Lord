package game.world.enemy;

import game.logic.EventBus;
import game.map.MapData;
import game.world.Player;

public class SentryEnemy extends BaseEnemy {
    private float turnRateDegPerSec = 0f;

    public SentryEnemy() { super("sentry"); }

    public void setTurnRate(float degPerSec) { this.turnRateDegPerSec = degPerSec; }
    public void setFov(float deg){ this.fovDeg = deg; }                 // NEW
    public void setViewDistTiles(float t){ this.viewDistTiles = t; }    // NEW

    @Override
    public void update(float dt, MapData map, Player player, EventBus events) {
        if (turnRateDegPerSec != 0f) {
            yaw = (yaw + turnRateDegPerSec * dt) % 360f;
        }
    }
}
