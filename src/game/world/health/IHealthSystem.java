package game.world.health;

public interface IHealthSystem {
    void applyHit(float amount, float severity);
    void tick(float dt);
    boolean isDead();
    boolean isCritical();
    void heal(int amount);
    void reset(long seed);
    float getSpeedMultiplier();
    float getAttackSpeedMultiplier();
    float getDamageMultiplier();
    String getStatusText();
    int getHp();
    int getMaxHp();

    default String getDamageStatus() { return null; }
    default String getInjuryStatus() { return null; }
    default String getBleedStatus()  { return null; }

    default void treatInjury(float amount) {}

    default float getDamageAcc()   { return -1f; }
    default float getInjuriesAcc() { return -1f; }
    default float getBloodloss()   { return -1f; }
    default float getBleedRate()   { return  0f; }
}
