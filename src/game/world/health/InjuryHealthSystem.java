package game.world.health;

import java.util.Random;

public class InjuryHealthSystem implements IHealthSystem {

    private static final float DAMAGE_MAX      = 100;
    private static final float INJURY_MAX      = 99999f;
    private static final float DEATH_THRESHOLD = 100f;

    private static final float DAMAGE_BLEED_THRESHOLD = 20f;
    private static final float DAMAGE_BLEED_CURVE = 0.0003f;
    private static final float INJURY_BLEED_RATE  = 0.002f;

    private static final float INJURY_BASE_CHANCE    = 0.04f;
    private static final float INJURY_DAMAGE_SCALING = 4.0f;

    private static final float DAMAGE_MILD_THRESHOLD   = 20f;
    private static final float DAMAGE_MOD_THRESHOLD    = 50f;
    private static final float DAMAGE_HEAVY_THRESHOLD  = 75f;
    private static final float INJURY_MILD_THRESHOLD   = 20f;
    private static final float INJURY_MOD_THRESHOLD    = 50f;
    private static final float INJURY_HEAVY_THRESHOLD  = 75f;

    private float damage    = 0f;
    private float injuries  = 0f;
    private float bloodloss = 0f;

    private final Random rng = new Random();

    @Override
    public void applyHit(float amount, float severity) {
        damage = Math.min(DAMAGE_MAX, damage + amount);

        float injuryChance = INJURY_BASE_CHANCE * severity * (1f + (damage / DAMAGE_MAX) * INJURY_DAMAGE_SCALING);
        if (rng.nextFloat() < injuryChance) {
            injuries = Math.min(INJURY_MAX, injuries + amount * 0.4f);
        }
    }

    @Override
    public void tick(float dt) {
        float excess      = Math.max(0f, damage - DAMAGE_BLEED_THRESHOLD);
        float damageBleed = excess * excess * DAMAGE_BLEED_CURVE;
        float injuryBleed = injuries * INJURY_BLEED_RATE;
        bloodloss += (damageBleed + injuryBleed) * dt;
    }

    @Override public boolean isDead()     { return bloodloss >= DEATH_THRESHOLD; }
    @Override public boolean isCritical() { return bloodloss > DEATH_THRESHOLD * 0.4f; }

    @Override
    public void heal(int amount) {
        damage = Math.max(0f, damage - amount);
    }

    @Override
    public void reset() {
        damage    = 0f;
        injuries  = 0f;
        bloodloss = 0f;
    }

    public void treatInjury(float amount) {
        injuries = Math.max(0f, injuries - amount);
    }

    @Override
    public float getSpeedMultiplier() {
        float m = 1.0f;
        if      (damage >= DAMAGE_HEAVY_THRESHOLD) m *= 0.75f;
        else if (damage >= DAMAGE_MOD_THRESHOLD)   m *= 0.85f;
        else if (damage >= DAMAGE_MILD_THRESHOLD)  m *= 0.95f;
        if      (injuries >= INJURY_HEAVY_THRESHOLD) m *= 0.60f;
        else if (injuries >= INJURY_MOD_THRESHOLD)   m *= 0.75f;
        else if (injuries >= INJURY_MILD_THRESHOLD)  m *= 0.90f;
        return m;
    }

    @Override
    public float getAttackSpeedMultiplier() {
        if (injuries >= INJURY_HEAVY_THRESHOLD) return 0.65f;
        if (injuries >= INJURY_MOD_THRESHOLD)   return 0.80f;
        if (injuries >= INJURY_MILD_THRESHOLD)  return 0.90f;
        return 1.0f;
    }

    @Override
    public float getDamageMultiplier() {
        if (injuries >= INJURY_HEAVY_THRESHOLD) return 0.75f;
        if (injuries >= INJURY_MOD_THRESHOLD)   return 0.85f;
        if (injuries >= INJURY_MILD_THRESHOLD)  return 0.95f;
        return 1.0f;
    }

    @Override
    public String getDamageStatus() {
        if (damage >= 150f) return "You are completely cut up";
        if (damage >= 100f) return "You are lacerated";
        if (damage >= 60f)  return "You are seriously wounded";
        if (damage >= 40f)  return "You have some cuts";
        return null;
    }

    @Override
    public String getInjuryStatus() {
        if (injuries >= 75f) return "You are severely injured";
        if (injuries >= 50f) return "You are limping";
        if (injuries >= 20f) return "You are a bit hurt";
        if (injuries >  0f)  return "You are superficially injured";
        return null;
    }

    @Override
    public String getBleedStatus() {
        float rate = getBleedRate();
        if (rate >= 2.0f)  return "You are deathly hurt";
        if (rate >= 0.5f)  return "You are bleeding heavily";
        if (rate >= 0.05f) return "You are bleeding";
        return null;
    }

    @Override
    public String getStatusText() {
        String d = getDamageStatus(), inj = getInjuryStatus(), b = getBleedStatus();
        if (d == null && inj == null && b == null) return null;
        StringBuilder sb = new StringBuilder();
        if (d   != null) sb.append(d);
        if (inj != null) { if (sb.length() > 0) sb.append(" | "); sb.append(inj); }
        if (b   != null) { if (sb.length() > 0) sb.append(" | "); sb.append(b); }
        return sb.toString();
    }

    @Override public int getHp()    { return -1; }
    @Override public int getMaxHp() { return -1; }

    @Override public float getDamageAcc()   { return damage; }
    @Override public float getInjuriesAcc() { return injuries; }
    @Override public float getBloodloss()   { return bloodloss; }
    @Override public float getBleedRate() {
        float excess = Math.max(0f, damage - DAMAGE_BLEED_THRESHOLD);
        return excess * excess * DAMAGE_BLEED_CURVE + injuries * INJURY_BLEED_RATE;
    }
}
