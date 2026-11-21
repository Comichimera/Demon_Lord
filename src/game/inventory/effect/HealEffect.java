package game.inventory.effect;

import game.inventory.ItemEffect;
import game.world.Player;

public class HealEffect implements ItemEffect {
    private final int amount;

    public HealEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.heal(amount);
    }
}
