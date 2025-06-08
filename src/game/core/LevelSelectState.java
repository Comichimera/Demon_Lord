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
            game.levelManager.advanceToLevel(choice);
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
