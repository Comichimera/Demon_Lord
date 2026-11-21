package game.world.health;

public class ClassicHealthSystem implements IHealthSystem {
    private int hp;
    private final int maxHp;

    public ClassicHealthSystem(int maxHp) {
        this.maxHp = maxHp;
        this.hp    = maxHp;
    }

    @Override public void applyHit(float amount, float severity) { hp = Math.max(0, hp - (int) Math.ceil(amount)); }
    @Override public void tick(float dt) {}
    @Override public boolean isDead()     { return hp <= 0; }
    @Override public boolean isCritical() { return hp < maxHp * 0.3f; }
    @Override public void heal(int amount)  { hp = Math.min(maxHp, hp + amount); }
    @Override public void reset()           { hp = maxHp; }

    @Override public float getSpeedMultiplier()       { return 1.0f; }
    @Override public float getAttackSpeedMultiplier() { return 1.0f; }
    @Override public float getDamageMultiplier()      { return 1.0f; }
    @Override public String getStatusText()           { return null; }

    @Override public int getHp()    { return hp; }
    @Override public int getMaxHp() { return maxHp; }
}
