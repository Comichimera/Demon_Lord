package game.core;

import game.ui.LevelCompleteOverlay;

import static org.lwjgl.glfw.GLFW.*;

public class LevelCompleteState implements IGameState {
    private final Game game;
    private final LevelCompleteOverlay overlay;
    private boolean enterPressed = false;

    public LevelCompleteState(Game game) {
        this.game = game;
        // Build the “Level Complete” text exactly as in original Game.updateGame:
        String lvlTime = game.formatTimeNoDecimal(game.elapsedTime);
        String cumTime = game.formatTimeNoDecimal(game.totalTime += game.elapsedTime);
        String target  = game.levelManager.getCurrentLevel().target;
        String upcoming = game.levelManager.hasNextLevel()
                ? game.levelManager.getLevels()
                .get(game.levelManager.getCurrentIndex()+1).name
                : "None";

        String text =
                "Level Complete\n" +
                        "Time: " + lvlTime + "\n" +
                        "Target: " + target + "\n\n" +
                        "Total: " + cumTime + "\n\n" +
                        "Next: " + upcoming;

        this.overlay = new LevelCompleteOverlay(game.window, text);
    }

    @Override
    public void enter() {

    }

    @Override
    public void update(float dt) {
        int state = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ENTER);
        if (state == GLFW_PRESS && !enterPressed) {
            enterPressed = true;
            if (game.levelManager.advanceToNextLevel()) {
                game.changeState(new PlayingState(game));
            } else {
                game.changeState(new MainMenuState(game));
            }
        } else if (state == GLFW_RELEASE) {
            enterPressed = false;
        }
    }

    @Override
    public void render() {
        overlay.render();
    }

    @Override
    public void exit() {
        overlay.cleanup();
    }
}
