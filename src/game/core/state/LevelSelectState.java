package game.core.state;

import game.core.Game;
import game.core.IGameState;
import game.input.EAction;
import game.input.Input;
import game.ui.MenuOverlay;

import java.util.List;

public class LevelSelectState implements IGameState {
    private final Game game;
    private final MenuOverlay menu;

    public LevelSelectState(Game game) {
        this.game = game;
        List<String> names = game.levelManager.getLevels()
                .stream().map(l -> l.name).toList();
        this.menu = new MenuOverlay(game.window, names);
    }

    @Override
    public void enter() {
        menu.rebuild();
    }

    @Override
    public void update(float dt) {
        if (Input.pressed(EAction.BACK)) {
            game.changeState(new MainMenuState(game));
            return;
        }

        int choice = menu.update();
        if (choice != -1) {
            game.levelManager.advanceToLevel(choice);

            if (choice == 0) {
                game.timers.startRun();
            }
            game.player.getInventory().clear();
            game.markLevelStart();
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
