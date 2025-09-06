package game.world.enemy;

import game.logic.EventBus;
import game.map.MapData;
import game.world.Player;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class PatrollerEnemy extends BaseEnemy {
    private final List<Vector2f> pathPoints = new ArrayList<>();
    private int   pathIndex = 0;
    private int   pathDir   = +1;     // +1 forward, -1 backward
    private float arriveEps = 0.1f * game.config.GameConfig.TILE_SIZE;

    public PatrollerEnemy() { super("patroller"); }

    public void setFov(float deg){ this.fovDeg = deg; }          // NEW
    public void setViewDistTiles(float t){ this.viewDistTiles = t; }

    public void addPathPointGrid(int gx, int gy) {
        pathPoints.add(new Vector2f(toWorldCenter(gx), toWorldCenter(gy)));
    }

    public List<Vector2f> getPathPoints() { return pathPoints; }

    @Override
    public void update(float dt, MapData map, Player player, EventBus events) {
        if (canSeePlayer(map, player)) {
            final float dx = player.getX() - x;
            final float dz = player.getZ() - z;
            final float dist = (float)Math.sqrt(dx*dx + dx*dz);
            if (dist > 1e-4f && speed > 0f) {
                faceToward(player.getX(), player.getZ());
                final float nx = dx / dist;
                final float nz = dz / dist;
                move(nx * speed * dt, nz * speed * dt, map);
            }
            return;
        }

        if (pathPoints.isEmpty()) return;

        Vector2f target = pathPoints.get(pathIndex);
        float dx = target.x - x;
        float dz = target.y - z;
        float dist = (float)Math.sqrt(dx*dx + dz*dz);

        // rotate to face our movement direction
        if (dist > 1e-4f) {
            float heading = (float)Math.toDegrees(Math.atan2(dx, dz)); // XZ plane
            this.yaw = heading;
        }

        // if close enough, switch to next node (ping-pong)
        if (dist <= arriveEps) {
            pathIndex += pathDir;
            if (pathIndex >= pathPoints.size()) { pathIndex = pathPoints.size() - 2; pathDir = -1; }
            if (pathIndex < 0)                  { pathIndex = 1;                  pathDir = +1; }
            target = pathPoints.get(pathIndex);
            dx = target.x - x; dz = target.y - z; dist = (float)Math.sqrt(dx*dx + dz*dz);
        }

        if (dist > 1e-4f && speed > 0f) {
            float nx = dx / dist;
            float nz = dz / dist;
            move(nx * speed * dt, nz * speed * dt, map); // reuse collision
        }
    }
}
