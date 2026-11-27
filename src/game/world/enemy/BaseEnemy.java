package game.world.enemy;

import game.config.GameConfig;
import game.map.MapData;
import game.physics.CollisionHandler;

public abstract class BaseEnemy {

    protected final String type;

    protected float x;
    protected float z;
    protected float yaw;

    protected float prevX, prevZ;

    protected float speed = 1.5f;
    protected float fovDeg = 90.0f;
    protected float viewDistTiles = 6.0f;
    protected float captureRadiusMeters = 0.45f * GameConfig.TILE_SIZE;

    protected int maxHp = 30;
    protected int hp    = 30;
    protected int attackDamage = 15;
    protected float attackCooldown = 0f;
    protected static final float ATTACK_RATE = 0.8f;

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
        prevX = worldX;
        prevZ = worldZ;
    }

    public void savePrevious() {
        prevX = x;
        prevZ = z;
    }

    public float lerpX(float a) {
        return prevX + (x - prevX) * a;
    }

    public float lerpZ(float a) {
        return prevZ + (z - prevZ) * a;
    }

    public void setYaw(float degrees) {
        this.yaw = normalizeDeg(degrees);
    }

    public void setSpeed(float metersPerSec) { this.speed = metersPerSec; }
    public void setFovDeg(float fovDeg) { this.fovDeg = fovDeg; }
    public void setViewDistTiles(float tiles) { this.viewDistTiles = tiles; }
    public void setCaptureRadiusMeters(float r) { this.captureRadiusMeters = r; }

    public int  getHp()           { return hp; }
    public int  getMaxHp()        { return maxHp; }
    public int  getAttackDamage() { return attackDamage; }
    public boolean isAlive()      { return hp > 0; }

    public void initHp(int max)          { this.maxHp = max; this.hp = max; }
    public void setAttackDamage(int dmg) { this.attackDamage = dmg; }

    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    public boolean tryAttack(float dt) {
        attackCooldown -= dt;
        if (attackCooldown <= 0f) {
            attackCooldown = ATTACK_RATE;
            return true;
        }
        return false;
    }
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
        return !CollisionHandler.isColliding(worldX, worldZ, map);
    }
}
