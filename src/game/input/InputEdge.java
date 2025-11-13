package game.input;

import static org.lwjgl.glfw.GLFW.*;

public final class InputEdge {
    private static final int[] UI_KEYS = {
            GLFW_KEY_ENTER, GLFW_KEY_SPACE, GLFW_KEY_ESCAPE, GLFW_KEY_BACKSPACE,
            GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_W, GLFW_KEY_S
    };

    private static boolean gate = false;                 // ignore until released after a state switch
    private static final boolean[] prev = new boolean[GLFW_KEY_LAST + 1];

    public static void onStateEnter(long window) {
        gate = true;
        // snapshot current states so nothing edges until a clean release
        for (int k : UI_KEYS) prev[k] = glfwGetKey(window, k) == GLFW_PRESS;
    }

    public static boolean pressedOnce(long window, int key) {
        boolean now = glfwGetKey(window, key) == GLFW_PRESS;
        boolean once = now && !prev[key] && !gate;
        prev[key] = now;

        // auto-disarm the gate once ALL UI keys are released
        if (gate) {
            boolean anyDown = false;
            for (int k : UI_KEYS) {
                if (glfwGetKey(window, k) == GLFW_PRESS) { anyDown = true; break; }
            }
            if (!anyDown) gate = false;
        }
        return once;
    }

    public static boolean anyPressedOnce(long window, int... keys) {
        for (int k : keys) if (pressedOnce(window, k)) return true;
        return false;
    }

    private InputEdge() {}
}
