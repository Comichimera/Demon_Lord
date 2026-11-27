package game.ui;

import game.config.GameConfig;
import game.core.Window;
import game.graphics.TextTexture;
import game.input.EAction;
import game.input.Input;
import game.util.FontLoader;
import game.util.Log;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class MenuOverlay {
    private final OverlayRenderer renderer;
    private final Window window;
    private final Font font;
    private final List<String> items;
    private int selected = 0;
    private TextTexture tex;
    private String lastText = "";
    private final TextTexture creditsTex;

    public MenuOverlay(Window window, List<String> items) {
        this(window, items, true);
    }

    public MenuOverlay(Window window, List<String> items, boolean showCredits) {
        this.renderer = new OverlayRenderer(window);
        this.window   = window;
        this.font     = FontLoader.menu( 24f);
        this.items    = items;
        this.creditsTex = showCredits ? loadCredits() : null;
        rebuild();
    }

    private TextTexture loadCredits() {
        try (InputStream is = MenuOverlay.class.getResourceAsStream(GameConfig.path("credits","/data/credits.txt"))) {
            if (is == null) return null;
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8).strip();
            Font creditsFont = FontLoader.menu( 14f);
            return new TextTexture(text, creditsFont, 6, 4, new Color(1f, 1f, 1f, 0.6f));
        } catch (Exception e) {
            Log.warn("Couldn't load credits", e);
            return null;
        }
    }

    public int update() {
        int prev = selected;

        if (Input.pressed(EAction.MENU_UP)) {
            selected = (selected + items.size() - 1) % items.size();
        }

        if (Input.pressed(EAction.MENU_DOWN)) {
            selected = (selected + 1) % items.size();
        }

        if (selected != prev) {
            rebuild();
        }

        if (Input.pressed(EAction.CONFIRM)) {
            return selected;
        }

        return -1;
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
        renderer.render(tex, UiScale.px(20), UiScale.menuTop(), true, 0.75f, 0f, 0f, 0f);
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
