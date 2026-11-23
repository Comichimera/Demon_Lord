package game.ui;

import game.config.GameConfig;
import game.core.Window;
import game.util.FontLoader;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class MenuOverlay {
    private final OverlayRenderer renderer;
    private final Window window;
    private final Font font;
    private final List<String> items;
    private int selected = 0;
    private TextTexture tex;
    private String lastText = "";
    private final TextTexture creditsTex;

    private boolean upPressed = false, downPressed = false, enterPressed = false;

    public MenuOverlay(Window window, List<String> items) {
        this.renderer = new OverlayRenderer(window);
        this.window   = window;
        this.font     = FontLoader.menu( 24f);
        this.items    = items;
        this.creditsTex = loadCredits();
        rebuild();
    }

    private TextTexture loadCredits() {
        try (InputStream is = MenuOverlay.class.getResourceAsStream(GameConfig.path("credits","/data/credits.txt"))) {
            if (is == null) return null;
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8).strip();
            Font creditsFont = FontLoader.menu( 14f);
            return new TextTexture(text, creditsFont, 6, 4, new Color(1f, 1f, 1f, 0.6f));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int update(long windowHandle, float deltaTime) {
        int prev = selected;

        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_PRESS && !upPressed) {
            upPressed = true;
            selected = (selected + items.size() - 1) % items.size();
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_RELEASE) {
            upPressed = false;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_PRESS && !downPressed) {
            downPressed = true;
            selected = (selected + 1) % items.size();
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_RELEASE) {
            downPressed = false;
        }

        if (selected != prev) {
            rebuild();
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_ENTER) == GLFW_PRESS && !enterPressed) {
            enterPressed = true;
            return selected;
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_ENTER) == GLFW_RELEASE) {
            enterPressed = false;
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
        if (creditsTex != null) {
            int x = window.getWidth() - creditsTex.getWidth() - 10;
            int y = window.getHeight() - creditsTex.getHeight() - 10;
            renderer.render(creditsTex, x, y, false, 0f, 0f, 0f, 0f);
        }
    }

    public void cleanup() {
        if (tex != null) tex.cleanup();
        if (creditsTex != null) creditsTex.cleanup();
    }
}
