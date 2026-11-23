package game.inventory.effect;

import game.inventory.IItemEffect;
import game.world.Player;

public class TreatInjuryEffect implements IItemEffect {
    private final float amount;

    public TreatInjuryEffect(float amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.treatInjury(amount);
    }
}
