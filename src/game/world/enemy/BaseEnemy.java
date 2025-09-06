package game.world.enemy;

import game.config.GameConfig;
import game.map.MapData;
import game.physics.CollisionHandler;
import game.world.Player;

public abstract class BaseEnemy implements Enemy {
    protected float x, y, z;
    protected float yaw;
    protected float speed = 1.5f;
    protected float fovDeg = 90f;
    protected float viewDistTiles = 6f;

    protected float captureRadiusMeters = 0.4f * GameConfig.TILE_SIZE;

    protected final String type;

    protected BaseEnemy(String type) {
        this.type = type;
        this.y   = 1.5f;
    }

    @Override public String getType() { return type; }
    @Override public float getX() { return x; }
    @Override public float getY() { return y; }
    @Override public float getZ() { return z; }
    @Override public float getYaw() { return yaw; }
    @Override public void setPosition(float x, float z) { this.x = x; this.z = z; }
    @Override public void setYaw(float yaw) { this.yaw = yaw; }

    protected void move(float dx, float dz, MapData map) {
        float nx = x + dx, nz = z + dz;
        if (!CollisionHandler.isColliding(nx, nz, map)) {
            x = nx; z = nz;
        }
    }

    @Override
    public boolean canSeePlayer(MapData map, Player player) {
        final float dx = player.getX() - x;
        final float dz = player.getZ() - z;
        final float tile = GameConfig.TILE_SIZE;
        final float maxDist = viewDistTiles * tile;
        final float dist2 = dx*dx + dz*dz;
        if (dist2 > maxDist * maxDist) return false;

        final double yawRad = Math.toRadians(yaw);
        final float fx = (float)Math.sin(yawRad);
        final float fz = (float)Math.cos(yawRad);
        final float len = (float)Math.sqrt(dx*dx + dz*dz);
        if (len < 1e-5f) return true;

        final float toX = dx / len;
        final float toZ = dz / len;

        final float dot = fx*toX + fz*toZ;
        final float cosHalfFov = (float)Math.cos(Math.toRadians(fovDeg * 0.5f));
        if (dot < cosHalfFov) return false;

        return hasLineOfSight(map, player);
    }

    protected void faceToward(float tx, float tz) {
        float dx = tx - x;
        float dz = tz - z;
        if (Math.abs(dx) + Math.abs(dz) > 1e-5f) {
            this.yaw = (float)Math.toDegrees(Math.atan2(dx, dz));
        }
    }

    protected boolean hasLineOfSight(MapData map, Player player) {
        final float tile = GameConfig.TILE_SIZE;
        int x0 = (int)(this.x / tile);
        int y0 = (int)(this.z / tile);
        int x1 = (int)(player.getX() / tile);
        int y1 = (int)(player.getZ() / tile);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int cx = x0, cy = y0;
        while (true) {
            if (!(cx == x0 && cy == y0)) {
                if (cx < 0 || cy < 0 || cx >= map.getWidth() || cy >= map.getHeight()) return false;
                if (!map.getTile(cy, cx).isWalkable()) return false; // wall/door blocks vision
            }
            if (cx == x1 && cy == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 <  dx) { err += dx; cy += sy; }
        }
        return true;
    }

    protected static float toWorldCenter(int grid) {
        return (grid + 0.5f) * GameConfig.TILE_SIZE;
    }

    public float getCaptureRadiusMeters() { return captureRadiusMeters; }
}