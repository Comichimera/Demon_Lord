package game.core;

import game.input.InputEdge;
import game.input.InputGate;
import game.ui.AchievementsOverlay;
import static org.lwjgl.glfw.GLFW.*;

public class AchievementsState implements IGameState {
    private final Game game;
    private AchievementsOverlay overlay;

    private boolean anyPressed = false;

    public AchievementsState(Game game) { this.game = game; }

    @Override public void enter() { overlay = new AchievementsOverlay(game.window); }

    @Override
    public void update(float dt) {
        long win = game.window.getWindowHandle();
        if (InputGate.isGated(win)) return;

        boolean pressed = glfwGetKey(win, GLFW_KEY_ESCAPE) == GLFW_PRESS
                || glfwGetKey(win, GLFW_KEY_ENTER)  == GLFW_PRESS
                || glfwGetKey(win, GLFW_KEY_SPACE)  == GLFW_PRESS
                || glfwGetKey(win, GLFW_KEY_BACKSPACE) == GLFW_PRESS;

        if (InputEdge.anyPressedOnce(win, GLFW_KEY_ENTER, GLFW_KEY_SPACE, GLFW_KEY_ESCAPE, GLFW_KEY_BACKSPACE)) {
            game.changeState(new MainMenuState(game));
            return;
        }
        if (!pressed) anyPressed = false;
    }

    @Override public void render() { overlay.render(); }

    @Override public void exit()   { overlay.cleanup(); }
}
