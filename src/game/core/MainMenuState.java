package game.core;

import game.achievements.AchievementsManager;
import game.audio.AudioSystem;
import game.input.InputGate;
import game.ui.MenuOverlay;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class MainMenuState implements IGameState {
    private final Game game;
    private final MenuOverlay menu;

    public MainMenuState(Game game) {
        this.game = game;
        this.menu = new MenuOverlay(
                game.window,
                List.of("Start Game", "Level Select", "Achievements", "Exit")
        );
    }

    @Override
    public void enter() {
        menu.rebuild();
        AudioSystem.playMusic("menu");
    }

    @Override
    public void update(float dt) {
        if (InputGate.isGated(game.window.getWindowHandle())) return;
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
                    game.changeState(new AchievementsState(game));
                    break;
                case 3:
                    AchievementsManager.get().unlock("go_outside");
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
        AudioSystem.stopMusic();
        menu.cleanup();
    }
}
