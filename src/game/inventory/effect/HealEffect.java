package game.inventory.effect;

import game.inventory.IItemEffect;
import game.world.Player;

public class HealEffect implements IItemEffect {
    private final int amount;

    public HealEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.heal(amount);
    }
}
