package game.input;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private static final EAction[] ACTIONS = EAction.values();
    private static final int[] keys = new int[ACTIONS.length];
    private static final int[] altKeys = new int[ACTIONS.length];
    private static final boolean[] down = new boolean[ACTIONS.length];
    private static final boolean[] wasDown = new boolean[ACTIONS.length];

    static {
        for (EAction a : ACTIONS) {
            keys[a.ordinal()] = a.defaultKey;
            altKeys[a.ordinal()] = a.defaultAltKey;
        }
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
}
