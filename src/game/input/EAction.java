package game.input;

import static org.lwjgl.glfw.GLFW.*;

public enum EAction {

    MOVE_FORWARD("Move forward", GLFW_KEY_W),
    MOVE_BACK("Move back", GLFW_KEY_S),
    TURN_LEFT("Turn left", GLFW_KEY_A),
    TURN_RIGHT("Turn right", GLFW_KEY_D),
    ATTACK("Attack", GLFW_KEY_F),
    INTERACT("Interact", GLFW_KEY_E),
    USE_ITEM("Use item", GLFW_KEY_G),
    NEXT_ITEM("Next Item", GLFW_KEY_TAB),
    PAUSE("Pause", GLFW_KEY_ESCAPE),
    DEBUG("Debug", GLFW_KEY_F3),

    MENU_UP("Menu up", GLFW_KEY_UP, GLFW_KEY_W),
    MENU_DOWN("Menu down", GLFW_KEY_DOWN, GLFW_KEY_S),
    MENU_LEFT("Menu left", GLFW_KEY_LEFT, GLFW_KEY_A),
    MENU_RIGHT("Menu right", GLFW_KEY_RIGHT, GLFW_KEY_D),
    CONFIRM("Confirm", GLFW_KEY_ENTER, GLFW_KEY_SPACE),
    BACK("Back", GLFW_KEY_ESCAPE, GLFW_KEY_BACKSPACE);

    public final String label;
    public final int defaultKey;
    public final int defaultAltKey;

    EAction(String label, int key) {
        this(label, key, GLFW_KEY_UNKNOWN);
    }

    EAction(String label, int key, int altKey) {
        this.label = label;
        this.defaultKey = key;
        this.defaultAltKey = altKey;
    }
}
