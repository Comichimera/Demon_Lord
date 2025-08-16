package game.core;

import game.ui.MenuOverlay;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class LevelSelectState implements IGameState {
    private final Game game;
    private final MenuOverlay menu;
    private boolean ignoreFirstInput = true;

    public LevelSelectState(Game game) {
        this.game = game;
        List<String> names = game.levelManager.getLevels()
                .stream().map(l -> l.name).toList();
        this.menu = new MenuOverlay(game.window, names);
    }

    @Override
    public void enter() {
        menu.rebuild();
        ignoreFirstInput = true;
    }

    @Override
    public void update(float dt) {
        if (ignoreFirstInput) {
            int state = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ENTER);
            if (state == GLFW_RELEASE) {
                ignoreFirstInput = false;
            }
            return;
        }

        int choice = menu.update(game.window.getWindowHandle(), dt);
        if (choice != -1) {
            // Move the cursor to the chosen level
            game.levelManager.advanceToLevel(choice);

            // If the player picked the FIRST level, treat it as a fresh run
            if (choice == 0) {
                // Either way is fine:
                // game.levelManager.reset();          // (not needed if you already advanced to 0)
                game.timers.startRun();                // <-- reset cumulative run time
            }

            game.changeState(new PlayingState(game));
        }
    }

    @Override
    public void render() {
        menu.render();
    }

    @Override
    public void exit() {
        menu.cleanup();
    }
}
