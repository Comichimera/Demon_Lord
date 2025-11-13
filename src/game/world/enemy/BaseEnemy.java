package game.world.enemy;

import game.config.GameConfig;
import game.map.MapData;

public abstract class BaseEnemy {

    protected final String type;

    protected float x;   // world X (meters)
    protected float z;   // world Z (meters)
    protected float yaw; // facing, degrees [0..360)

    protected float speed = 1.5f;          // meters/sec (base move speed)
    protected float fovDeg = 90.0f;        // field of view (for LOS helpers / sensors)
    protected float viewDistTiles = 6.0f;  // vision distance in tiles (sensor consumes)
    protected float captureRadiusMeters = 0.45f * GameConfig.TILE_SIZE;

    protected BaseEnemy(String type) {
        this.type = type;
    }

    public String getType() { return type; }

    public float getX() { return x; }
    public float getZ() { return z; }
    public float getYaw() { return yaw; }

    public float getSpeed() { return speed; }
    public float getFovDeg() { return fovDeg; }
    public float getViewDistTiles() { return viewDistTiles; }

    public float getCaptureRadiusMeters() { return captureRadiusMeters; }

    public void setPosition(float worldX, float worldZ) {
        this.x = worldX;
        this.z = worldZ;
    }

    public void setYaw(float degrees) {
        this.yaw = normalizeDeg(degrees);
    }

    public void setSpeed(float metersPerSec) { this.speed = metersPerSec; }
    public void setFovDeg(float fovDeg) { this.fovDeg = fovDeg; }
    public void setViewDistTiles(float tiles) { this.viewDistTiles = tiles; }
    public void setCaptureRadiusMeters(float r) { this.captureRadiusMeters = r; }
    protected void move(float dx, float dz, MapData map) {
        float newX = x + dx;
        float newZ = z + dz;

        if (isWalkable(map, newX, z)) x = newX;
        if (isWalkable(map, x, newZ)) z = newZ;
    }

    public void faceToward(float cx, float cz) {
        float dx = cx - x;
        float dz = cz - z;
        if (Math.abs(dx) > 1e-6f || Math.abs(dz) > 1e-6f) {
            this.yaw = normalizeDeg((float)Math.toDegrees(Math.atan2(-dz, dx)) + 90f);
        }
    }
    protected boolean hasLineOfSight(MapData map, int fromTileX, int fromTileY, int toTileX, int toTileY) {
        throw new UnsupportedOperationException("Provide existing LOS implementation here");
    }

    protected boolean withinFovAndRange(int selfTx, int selfTy, int tx, int ty) {
        float dx = (tx - selfTx);
        float dz = (ty - selfTy);
        float distTiles = Math.abs(dx) + Math.abs(dz);
        if (distTiles > viewDistTiles) return false;

        double toAngle = Math.toDegrees(Math.atan2(-dz, dx)) + 90.0;
        float delta = angleDelta((float)toAngle, yaw);
        return Math.abs(delta) <= (fovDeg * 0.5f);
    }

    protected int tileX() { return (int)(x / GameConfig.TILE_SIZE); }
    protected int tileY() { return (int)(z / GameConfig.TILE_SIZE); }

    private static float normalizeDeg(float a) {
        float r = a % 360f;
        return r < 0 ? r + 360f : r;
    }

    private static float angleDelta(float a, float b) {
        float d = ((a - b + 540f) % 360f) - 180f;
        return d;
    }

    protected boolean isWalkable(MapData map, float worldX, float worldZ) {
        return true;
    }
}
