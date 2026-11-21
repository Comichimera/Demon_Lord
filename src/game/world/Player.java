package game.world;

import game.config.GameConfig;
import game.inventory.Inventory;
import game.map.MapData;
import game.physics.CollisionHandler;
import game.world.health.ClassicHealthSystem;
import game.world.health.IHealthSystem;
import game.world.health.InjuryHealthSystem;

public class Player {
    private float x, y, z;
    private float yaw;
    private final Inventory inventory = new Inventory();

    private final int baseAttackDamage = 25;
    private final IHealthSystem health;

    public Player() {
        this.x   = 0.0f;
        this.y   = 1.5f;
        this.z   = 0.0f;
        this.yaw = 0.0f;
        this.health = (GameConfig.healthSystem == GameConfig.HealthSystem.CLASSIC)
                ? new ClassicHealthSystem(100)
                : new InjuryHealthSystem();
    }

    public void move(float dx, float dz, MapData mapData) {
        float newX = x + dx;
        float newZ = z + dz;
        if (!CollisionHandler.isColliding(newX, newZ, mapData)) {
            x = newX;
            z = newZ;
        }
    }

    public void rotate(float angle) { yaw += angle; }
    public void applyCameraTransform() {}

    public float getYaw() { return yaw; }
    public float getX()   { return x; }
    public float getY()   { return y; }
    public float getZ()   { return z; }

    public Inventory getInventory() { return inventory; }

    public void setYaw(float yaw)              { this.yaw = yaw; }
    public void setPosition(float x, float z)  { this.x = x; this.z = z; }

    public void tickHealth(float dt)  { health.tick(dt); }
    public boolean isAlive()          { return !health.isDead(); }
    public boolean isHealthCritical() { return health.isCritical(); }

    public void applyHit(float amount, float severity) { health.applyHit(amount, severity); }
    public void takeDamage(int amount)                 { health.applyHit(amount, 1.0f); }
    public void heal(int amount)                       { health.heal(amount); }
    public void treatInjury(float amount)              { health.treatInjury(amount); }
    public void resetHealth()                          { health.reset(); }

    public float getSpeedMultiplier()       { return health.getSpeedMultiplier(); }
    public float getAttackSpeedMultiplier() { return health.getAttackSpeedMultiplier(); }
    public float getDamageMultiplier()      { return health.getDamageMultiplier(); }
    public String getStatusText()            { return health.getStatusText(); }
    public String getDamageStatus()          { return health.getDamageStatus(); }
    public String getInjuryStatus()          { return health.getInjuryStatus(); }
    public String getBleedStatus()           { return health.getBleedStatus(); }

    public int getAttackDamage() { return baseAttackDamage; }
    public int getHp()           { return health.getHp(); }
    public int getMaxHp()        { return health.getMaxHp(); }

    public float getDamageAcc()   { return health.getDamageAcc(); }
    public float getInjuriesAcc() { return health.getInjuriesAcc(); }
    public float getBloodloss()   { return health.getBloodloss(); }
    public float getBleedRate()   { return health.getBleedRate(); }
}
