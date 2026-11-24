package game.inventory;

import game.core.Game;
import game.input.InputEdge;
import game.map.Tile;
import game.ui.InventoryOverlay;
import game.ui.TimerOverlay;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

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
        if (InputEdge.pressedOnce(game.window.getWindowHandle(), GLFW_KEY_TAB)) {
            game.player.getInventory().selectNext();
            inventoryOverlay.rebuild(game.player.getInventory());
        }

        if (InputEdge.pressedOnce(game.window.getWindowHandle(), GLFW_KEY_G) &&  game.player.isAlive()) {
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
                    System.out.println("Picked up item '" + itemId + "'");

                    if (inventoryOverlay != null) {
                        inventoryOverlay.rebuild(game.player.getInventory());
                    }

                    game.renderer.updateFloorGeometry();
                } else {
                    System.out.println("Inventory full, could not pick up '" + itemId + "'");
                }
            }
        }
    }
}
