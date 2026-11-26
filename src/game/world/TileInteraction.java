package game.world;

import game.core.Game;
import game.input.EAction;
import game.input.Input;
import game.map.Tile;
import game.ui.InventoryOverlay;
import game.ui.TimerOverlay;

public class TileInteraction {

    // continue here

    private final Game game;
    private final InventoryOverlay inventoryOverlay;
    private final TimerOverlay timer;

    public TileInteraction(Game game, InventoryOverlay inventoryOverlay, TimerOverlay timer) {
        this.game = game;
        this.inventoryOverlay = inventoryOverlay;
        this.timer = timer;
    }

    public void update(int tx, int ty) {
        if (Input.pressed(EAction.INTERACT)) {

            int col = tx;
            int row = ty;
            int[][] offs = {{-1,0},{1,0},{0,-1},{0,1}};

            for (int[] off : offs) {
                int r = row + off[0], c = col + off[1];
                if (r >= 0 && r < game.mapData.getHeight() && c >= 0 && c < game.mapData.getWidth()) {
                    Tile t = game.mapData.getTile(r, c);
                    if (!t.isOpenable()) continue;

                    if (t.isLocked()) {
                        String usedKey = null;
                        for (String keyId : t.getLockedByKeys()) {
                            if (game.player.getInventory().hasItem(keyId)) {
                                usedKey = keyId;
                                break;
                            }
                        }
                        if (usedKey != null) {
                            game.player.getInventory().removeItem(usedKey, 1);
                            t.setLocked(false);
                            t.setOpen(true);
                            t.setWalkable(true);
                            game.renderer.updateFloorGeometry();
                            inventoryOverlay.rebuild(game.player.getInventory());
                            timer.pushPopup("Door unlocked!", 1.5f);
                        } else {
                            timer.pushPopup("Locked", 1.5f);
                        }
                    } else {
                        t.setOpen(!t.isOpen());
                        t.setWalkable(t.isOpen());
                        game.renderer.updateFloorGeometry();
                    }
                    break;
                }
            }
        }
    }
}
