package game.core;

import game.ui.MenuOverlay;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class MainMenuState implements IGameState {
    private final Game game;          // reference back to orchestrate transitions
    private final MenuOverlay menu;

    public MainMenuState(Game game) {
        this.game = game;
        this.menu = new MenuOverlay(
                game.window,
                List.of("Start Game", "Level Select", "Exit")
        );
    }

    @Override
    public void enter() {
        // Reset the menu’s internal “selected” index if needed.
        menu.rebuild();
    }

    @Override
    public void update(float dt) {
        // Query the overlay for a selection (Return value: 0=start,1=level-select,2=exit; -1=none)
        int choice = menu.update(game.window.getWindowHandle(), dt);
        if (choice != -1) {
            switch (choice) {
                case 0:
                    game.changeState(new PlayingState(game));
                    break;
                case 1:
                    game.changeState(new LevelSelectState(game));
                    break;
                case 2:
                    game.running = false;
                    break;
            }
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
