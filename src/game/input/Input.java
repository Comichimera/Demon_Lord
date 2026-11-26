package game.input;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private static final EAction[] ACTIONS = EAction.values();
    private static final int[] keys = new int[ACTIONS.length];
    private static final int[] altKeys = new int[ACTIONS.length];
    private static final boolean[] down = new boolean[ACTIONS.length];
    private static final boolean[] wasDown = new boolean[ACTIONS.length];

    private static int lastPressedKey = GLFW_KEY_UNKNOWN;

    static {
        resetToDefaults();
    }

    private Input() {}

    public static void update(long window) {
        for (int i = 0; i < ACTIONS.length; i++) {
            wasDown[i] = down[i];
            down[i] = isKeyDown(window, keys[i]) || isKeyDown(window, altKeys[i]);
        }
    }

    private static boolean isKeyDown(long window, int key) {
        if (key == GLFW_KEY_UNKNOWN) return false;
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public static boolean isDown(EAction action) {
        return down[action.ordinal()];
    }

    public static boolean pressed(EAction action) {
        int i = action.ordinal();
        return down[i] && !wasDown[i];
    }

    public static int getKey(EAction action) {
        return keys[action.ordinal()];
    }

    public static void setKey(EAction action, int key) {
        keys[action.ordinal()] = key;
    }

    public static void resetToDefaults() {
        for (EAction a : ACTIONS) {
            keys[a.ordinal()] = a.defaultKey;
            altKeys[a.ordinal()] = a.defaultAltKey;
        }
    }

    public static void onKeyPressed(int key) {
        lastPressedKey = key;
    }

    public static int takePressedKey() {
        int key = lastPressedKey;
        lastPressedKey = GLFW_KEY_UNKNOWN;
        return key;
    }

    public static String keyName(int key) {
        if (key >= GLFW_KEY_F1 && key <= GLFW_KEY_F25) return "F" + (key - GLFW_KEY_F1 + 1);
        return switch (key) {
            case GLFW_KEY_UNKNOWN -> "None";
            case GLFW_KEY_SPACE -> "Space";
            case GLFW_KEY_ESCAPE -> "Esc";
            case GLFW_KEY_ENTER -> "Enter";
            case GLFW_KEY_TAB -> "Tab";
            case GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW_KEY_UP -> "Up";
            case GLFW_KEY_DOWN -> "Down";
            case GLFW_KEY_LEFT -> "Left";
            case GLFW_KEY_RIGHT -> "Right";
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> "Shift";
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> "Ctrl";
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> "Alt";
            default -> {
                String name = glfwGetKeyName(key, 0);
                yield name != null ? name.toUpperCase() : "Key " + key;
            }
        };
    }

}
