package game.inventory;

import game.core.Game;
import game.input.EAction;
import game.input.Input;
import game.map.Tile;
import game.ui.InventoryOverlay;
import game.ui.TimerOverlay;
import game.util.Log;

public class InventoryController {

    private final Game game;
    private final InventoryOverlay inventoryOverlay;
    private final TimerOverlay timer;

    public InventoryController(Game game, InventoryOverlay inventoryOverlay, TimerOverlay timer) {
        this.game = game;
        this.inventoryOverlay = inventoryOverlay;
        this.timer = timer;
    }

    public void handleKeys() {
        if (Input.pressed(EAction.NEXT_ITEM)) {
            game.player.getInventory().selectNext();
            inventoryOverlay.rebuild(game.player.getInventory());
        }

        if (Input.pressed(EAction.USE_ITEM) &&  game.player.isAlive()) {
            InventoryEntry selected = game.player.getInventory().getSelectedEntry();
            if (selected != null) {
                ItemDefinition def = selected.getDefinition();
                if (!def.getEffects().isEmpty()) {
                    for (IItemEffect effect : def.getEffects()) {
                        effect.apply(game.player);
                    }
                    game.player.getInventory().removeItem(def.getId(), 1);
                    inventoryOverlay.rebuild(game.player.getInventory());
                    timer.pushPopup("Used: " + def.getName(), 1.5f);
                }
            }
        }
    }



    public void handlePickups(int ty, int tx) {
        if (ty >= 0 && ty < game.mapData.getHeight()
                && tx >= 0 && tx < game.mapData.getWidth()) {

            Tile current = game.mapData.getTile(ty, tx);

            if (current.hasItem()) {
                String itemId = current.getItemId();

                boolean added = game.player.getInventory().addItem(itemId, 1);
                if (added) {
                    current.clearItem();
                    Log.info("Picked up item '" + itemId + "'");

                    if (inventoryOverlay != null) {
                        inventoryOverlay.rebuild(game.player.getInventory());
                    }

                    game.renderer.updateFloorGeometry();
                } else {
                    Log.info("Inventory full, could not pick up '" + itemId + "'");
                }
            }
        }
    }
}
