package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TimerOverlay {
    private final OverlayRenderer renderer;
    private final Font font;

    private TextTexture timeTex;
    private String lastTimeStr = "";
    private double lastElapsedForDt = 0.0;

    private static final class Toast {
        final String text;
        float ttl;
        final TextTexture tex;
        Toast(String text, float ttl, TextTexture tex) {
            this.text = text; this.ttl = ttl; this.tex = tex;
        }
    }
    private final List<Toast> toasts = new ArrayList<>();

    public TimerOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/arial.ttf", 16f);
    }

    public String formatTime(double elapsedTime) {
        int minutes = (int)(elapsedTime / 60);
        double seconds = elapsedTime - minutes * 60;
        if (seconds >= 59.95) { seconds = 0; minutes++; }
        return String.format("%d:%04.1f", minutes, seconds);
    }

    public void update(double elapsed) {
        String s = formatTime(elapsed);
        if (!s.equals(lastTimeStr)) {
            if (timeTex != null) timeTex.cleanup();
            timeTex = new TextTexture(s, font, 10, 5, Color.WHITE);
            lastTimeStr = s;
        }

        float dt = (float)Math.max(0.0, elapsed - lastElapsedForDt);
        lastElapsedForDt = elapsed;

        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            t.ttl -= dt;
            if (t.ttl <= 0f) {
                t.tex.cleanup();
                toasts.remove(i);
            }
        }
    }

    public void pushPopup(String text, float seconds) {
        if (text == null || text.isEmpty()) return;
        float ttl = Math.max(0.5f, seconds);
        TextTexture popupTex = new TextTexture(text, font, 10, 5, Color.WHITE);
        toasts.add(new Toast(text, ttl, popupTex));
    }

    public void render() {
        if (timeTex != null) {
            int x = renderer.window.getWidth() - timeTex.getWidth() - 10;
            int y = 10;
            renderer.render(timeTex, x, y, false, 0, 0, 0, 0);
        }

        final int startX = 12;
        int y = 48;
        for (int i = 0; i < toasts.size(); i++) {
            Toast t = toasts.get(i);
            TextTexture tex = t.tex;
            renderer.render(tex, startX, y, false, 0, 0, 0, 0);
            y += tex.getHeight() + 6;
        }
    }

    public void cleanup() {
        if (timeTex != null) timeTex.cleanup();
        for (Toast t : toasts) t.tex.cleanup();
        toasts.clear();
    }
}
