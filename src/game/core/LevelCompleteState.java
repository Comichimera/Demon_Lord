package game.core;

import game.logic.TimerService;
import game.ui.LevelCompleteOverlay;

import static org.lwjgl.glfw.GLFW.*;

public class LevelCompleteState implements IGameState {
    private final Game game;
    private LevelCompleteOverlay overlay;
    private boolean enterPressed = false;

    public LevelCompleteState(Game game) {
        this.game = game;
    }

    @Override
    public void enter() {
        // Commit the level exactly once on entering this state.
        game.timers.completeLevel();

        String lvlTime = TimerService.mmss(game.timers.levelSeconds());
        String cumTime = TimerService.mmss(game.timers.totalSeconds());
        String target  = game.levelManager.getCurrentLevel().target;
        String upcoming = game.levelManager.hasNextLevel()
                ? game.levelManager.getLevels().get(game.levelManager.getCurrentIndex()+1).name
                : "None";

        String text = "Level Complete\n" +
                "Time: " + lvlTime + "\n" +
                "Target: " + target + "\n\n" +
                "Total: " + cumTime + "\n\n" +
                "Next: " + upcoming;

        overlay = new LevelCompleteOverlay(game.window, text);
    }

    @Override
    public void update(float dt) {
        int state = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ENTER);
        if (state == GLFW_PRESS && !enterPressed) {
            enterPressed = true;

            if (game.levelManager.advanceToNextLevel()) {
                game.changeState(new PlayingState(game)); // PlayingState.enter() will startLevel()
            } else {
                game.changeState(new MainMenuState(game));
            }
        } else if (state == GLFW_RELEASE) {
            enterPressed = false;
        }
    }

    @Override public void render() { overlay.render(); }
    @Override public void exit()   { overlay.cleanup(); }
}
