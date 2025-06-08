package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;

public class MenuOverlay {
    private final OverlayRenderer renderer;
    private final Font font;
    private final List<String> items;
    private int selected = 0;
    private TextTexture tex;
    private String lastText = "";

    private boolean upPressed = false, downPressed = false, enterPressed = false;

    public MenuOverlay(Window window, List<String> items) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/Rubik-Regular.ttf", 24f);
        this.items    = items;
        rebuild();
    }

    public int update(long windowHandle, float deltaTime) {
        int prev = selected;

        // UP arrow debounce
        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_PRESS && !upPressed) {
            upPressed = true;
            selected = (selected + items.size() - 1) % items.size();
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_RELEASE) {
            upPressed = false;
        }

        // DOWN arrow debounce
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_PRESS && !downPressed) {
            downPressed = true;
            selected = (selected + 1) % items.size();
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_RELEASE) {
            downPressed = false;
        }

        // only rebuild when selection actually changed
        if (selected != prev) {
            rebuild();
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_ENTER) == GLFW_PRESS && !enterPressed) {
            enterPressed = true;
            return selected;          // only fires once per press
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_ENTER) == GLFW_RELEASE) {
            enterPressed = false;     // reset so the next press can fire again
        }

        return -1;
    }

    public void activate(long windowHandle) {
        upPressed    = glfwGetKey(windowHandle, GLFW_KEY_UP)    == GLFW_PRESS;
        downPressed  = glfwGetKey(windowHandle, GLFW_KEY_DOWN)  == GLFW_PRESS;
        enterPressed = glfwGetKey(windowHandle, GLFW_KEY_ENTER) == GLFW_PRESS;
    }

    public void rebuild() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i == selected) sb.append("> ");
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append("\n");
        }
        String s = sb.toString();
        if (s.equals(lastText)) return;
        if (tex != null) tex.cleanup();
        tex = new TextTexture(s, font, 10, 5, Color.WHITE);
        lastText = s;
    }

    public void render() {
        renderer.render(tex, 20, 20, true, 0.75f, 0f, 0f, 0f);
    }

    public void cleanup() {
        if (tex != null) tex.cleanup();
    }
}
