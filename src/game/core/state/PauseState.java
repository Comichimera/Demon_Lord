package game.core.state;

import game.core.Game;
import game.core.IGameState;
import game.input.InputEdge;
import game.input.InputGate;
import game.ui.MenuOverlay;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class PauseState implements IGameState {

    private final Game game;
    private final IGameState level;
    private final MenuOverlay menu;

    public PauseState(Game game, IGameState level) {
        this.game = game;
        this.level = level;
        this.menu = new MenuOverlay(
                game.window,
                List.of("Resume", "Restart Level", "Settings", "Quit to Menu"),
                false
        );
    }

    @Override
    public void enter() {
        menu.rebuild();
    }

    @Override
    public void update(float dt) {
        long win = game.window.getWindowHandle();
        if(InputGate.isGated(win)) return;

        if (InputEdge.pressedOnce(win, GLFW_KEY_ESCAPE)) {
            resume();
            return;
        }

        int choice = menu.update(win);
        switch (choice) {
            case 0 -> resume();
            case 1 -> game.changeState(new PlayingState(game));
            case 2 -> game.pushState(new SettingsState(game));
            case 3 -> game.changeState(new MainMenuState(game));
            default -> { }
        }
    }

    private void resume() {
        game.timers.resume();
        game.popState();
    }

    @Override
    public void render() {
        level.render();
        menu.render();
    }

    @Override
    public void exit() {
        menu.cleanup();
    }
}
