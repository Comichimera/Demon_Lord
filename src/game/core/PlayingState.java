package game.core;

import game.input.KeyboardInput;
import game.ui.TimerOverlay;
import game.world.Player;
import game.map.LevelManager;
import game.config.GameConfig;
import game.graphics.Renderer;
import game.map.MapData;
import game.map.Tile;
import static org.lwjgl.glfw.GLFW.*;

public class PlayingState implements IGameState {
    private final Game game;
    private final KeyboardInput keyboard;
    private final TimerOverlay timer;
    private boolean interactPressed = false;


    public PlayingState(Game game) {
        this.game = game;
        this.keyboard = new KeyboardInput();
        this.timer    = new TimerOverlay(game.window);
    }

    @Override
    public void enter() {
        game.reloadLevel();
        timer.update(0);
    }

    @Override
    public void update(float dt) {
        if (glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            game.changeState(new MainMenuState(game));
            return;
        }

        game.elapsedTime += dt;
        timer.update(game.elapsedTime);

        // Process movement and interactions:
        keyboard.processInput(game.player, dt, game.mapData);

        // Interaction key ("E") toggles adjacent openable tiles (e.g., doors)
        int interactState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_E);
        if (interactState == GLFW_PRESS && !interactPressed) {
            interactPressed = true;

            int col = (int)(game.player.getX() / GameConfig.TILE_SIZE);
            int row = (int)(game.player.getZ() / GameConfig.TILE_SIZE);
            int[][] offs = {{-1,0},{1,0},{0,-1},{0,1}};  // NSEW

            for (int[] off : offs) {
                int r = row + off[0], c = col + off[1];
                if (r >= 0 && r < game.mapData.getHeight() && c >= 0 && c < game.mapData.getWidth()) {
                    Tile t = game.mapData.getTile(r, c);
                    if (t.isOpenable()) {
                        t.setOpen(!t.isOpen());
                        t.setWalkable(t.isOpen());
                        game.renderer.updateFloorGeometry();
                        break;
                    }
                }
            }
        } else if (interactState == GLFW_RELEASE) {
            interactPressed = false;
        }

        // Check if player is standing on an exit tile
        {
            int col = (int)(game.player.getX() / GameConfig.TILE_SIZE);
            int row = (int)(game.player.getZ() / GameConfig.TILE_SIZE);
            if (row >= 0 && row < game.mapData.getHeight()
                    && col >= 0 && col < game.mapData.getWidth()) {

                Tile t = game.mapData.getTile(row, col);
                if (t.isEndsLevel()) {

                    String lvlTime = game.formatTimeNoDecimal(game.elapsedTime);
                    String cumTime = game.formatTimeNoDecimal(game.totalTime);
                    String target = game.levelManager.getCurrentLevel().target;
                    String upcoming = (game.levelManager.hasNextLevel())
                            ? game.levelManager.getLevels()
                            .get(game.levelManager.getCurrentIndex() + 1).name
                            : "None";

                    String text =
                            "Level Complete\n" +
                                    "Time: " + lvlTime + "\n" +
                                    "Target: " + target + "\n\n" +
                                    "Total: " + cumTime + "\n\n" +
                                    "Next: " + upcoming;

                    game.changeState(new LevelCompleteState(game));
                }
            }
        }
    }

    @Override
    public void render() {
        // Render the 3D world (Player + map)
        game.renderer.render(game.player);
        // Render the timer overlay
        timer.render();
    }

    @Override
    public void exit() {
        timer.cleanup();
    }
}
