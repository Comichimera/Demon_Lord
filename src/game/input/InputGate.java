package game.input;

import static org.lwjgl.glfw.GLFW.*;

public final class InputGate {
    private static boolean gated = false;

    public static void arm() { gated = true; }

    public static boolean isGated(long window) {
        if (!gated) return false;
        boolean anyDown =
                glfwGetKey(window, GLFW_KEY_ENTER)     == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_SPACE)     == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_ESCAPE)    == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_BACKSPACE) == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_UP)        == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_DOWN)      == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_W)         == GLFW_PRESS ||
                        glfwGetKey(window, GLFW_KEY_S)         == GLFW_PRESS;
        if (!anyDown) gated = false;
        return gated;
    }

    private InputGate() {}
}
