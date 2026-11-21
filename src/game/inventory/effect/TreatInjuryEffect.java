package game.inventory.effect;

import game.inventory.ItemEffect;
import game.world.Player;

public class TreatInjuryEffect implements ItemEffect {
    private final float amount;

    public TreatInjuryEffect(float amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.treatInjury(amount);
    }
}
