package game.core.state;

import game.config.Settings;
import game.core.Game;
import game.core.IGameState;
import game.input.EAction;
import game.input.Input;
import game.ui.MenuOverlay;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class ControlsState implements IGameState {

    private static final EAction[] ACTIONS = {
            EAction.MOVE_FORWARD, EAction.MOVE_BACK,
            EAction.TURN_LEFT, EAction.TURN_RIGHT,
            EAction.ATTACK, EAction.INTERACT,
            EAction.USE_ITEM, EAction.NEXT_ITEM,
            EAction.PAUSE
    };

    private static final int RESET = ACTIONS.length;
    private static final int BACK = ACTIONS.length + 1;

    private final Game game;
    private final List<String> items = new ArrayList<>();
    private final MenuOverlay menu;
    private int waiting = -1;

    public ControlsState(Game game) {
        this.game = game;
        fillItems();
        this.menu = new MenuOverlay(game.window, items, false);
    }

    private void fillItems() {
        items.clear();
        for (int i = 0; i < ACTIONS.length; i++) {
            String key = (1 == waiting) ? "press a key..." : Input.keyName(Input.getKey(ACTIONS[i]));
            items.add(ACTIONS[i].label + ": " + key);
        }
        items.add("Reset to defaults");
        items.add("Back");
    }

    @Override
    public void enter() {
        menu.rebuild();
    }

    @Override
    public void update(float dt) {
        if (waiting != -1) {
            int key = Input.takePressedKey();
            if (key == GLFW_KEY_UNKNOWN) return;
            if (key != GLFW_KEY_ESCAPE) bind(ACTIONS[waiting], key);
            waiting = -1;
            fillItems();
            menu.rebuild();
            return;
        }

        if (Input.pressed(EAction.BACK)) {
            leave();
            return;
        }

        int choice = menu.update();
        if (choice == -1) return;

        if (choice == BACK) {
            leave();
            return;
        }

        if (choice == RESET) {
            Input.resetToDefaults();
        } else {
            waiting = choice;
            Input.takePressedKey();
        }

        fillItems();
        menu.rebuild();
    }

    private void bind(EAction action, int key) {
        int oldKey = Input.getKey(action);
        for (EAction other : ACTIONS) {
            if (other != action && Input.getKey(other) == key) {
                Input.setKey(other, oldKey);
            }
        }
        Input.setKey(action, key);
    }

    private void leave() {
        Settings.save();
        game.popState();
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
