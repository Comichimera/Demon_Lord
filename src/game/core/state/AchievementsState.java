package game.core.state;

import game.core.Game;
import game.core.IGameState;
import game.input.EAction;
import game.input.Input;
import game.ui.AchievementsOverlay;

public class AchievementsState implements IGameState {
    private final Game game;
    private AchievementsOverlay overlay;

    public AchievementsState(Game game) { this.game = game; }

    @Override public void enter() { overlay = new AchievementsOverlay(game.window); }

    @Override
    public void update(float dt) {

        if (Input.pressed(EAction.BACK) || Input.pressed(EAction.CONFIRM)) {
            game.changeState(new MainMenuState(game));
            return;
        }
    }

    @Override public void render() { overlay.render(); }

    @Override public void exit()   { overlay.cleanup(); }
}
