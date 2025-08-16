package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class TimerOverlay {
    private final OverlayRenderer renderer;
    private final Font font;

    //Timer text
    private TextTexture timeTex;
    private String lastTimeStr = "";
    private double lastElapsedForDt = 0.0; // used to derive dt each update

    // simple pop-up messages
    private static final class Toast {
        final String text;
        float ttl;              // seconds remaining
        final TextTexture tex;  // pre-baked text for the toast
        Toast(String text, float ttl, TextTexture tex) {
            this.text = text; this.ttl = ttl; this.tex = tex;
        }
    }
    private final List<Toast> toasts = new ArrayList<>();

    public TimerOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/RubikMonoOne.ttf", 20f);
    }

    // Format mm:ss.t
    public String formatTime(double elapsedTime) {
        int minutes = (int)(elapsedTime / 60);
        double seconds = elapsedTime - minutes * 60;
        if (seconds >= 59.95) { seconds = 0; minutes++; }
        return String.format("%d:%04.1f", minutes, seconds);
    }

    // Call once per frame with total elapsed seconds
    public void update(double elapsed) {
        // Update main timer texture when text changes
        String s = formatTime(elapsed);
        if (!s.equals(lastTimeStr)) {
            if (timeTex != null) timeTex.cleanup();
            timeTex = new TextTexture(s, font, 10, 5, Color.WHITE);
            lastTimeStr = s;
        }

        // Derive dt from elapsed
        float dt = (float)Math.max(0.0, elapsed - lastElapsedForDt);
        lastElapsedForDt = elapsed;

        // Age out toasts
        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast t = toasts.get(i);
            t.ttl -= dt;
            if (t.ttl <= 0f) {
                t.tex.cleanup();
                toasts.remove(i);
            }
        }
    }

    // Show a popup message for `seconds` (e.g., 2.5f).
    public void pushPopup(String text, float seconds) {
        if (text == null || text.isEmpty()) return;
        float ttl = Math.max(0.5f, seconds);
        // You can change color here if you want (e.g., green for objectives)
        TextTexture popupTex = new TextTexture(text, font, 10, 5, Color.WHITE);
        toasts.add(new Toast(text, ttl, popupTex));
    }

    public void render() {
        // Timer
        if (timeTex != null) {
            int x = renderer.window.getWidth() - timeTex.getWidth() - 10;
            int y = 10;
            renderer.render(timeTex, x, y, false, 0, 0, 0, 0);
        }

        // Toasts
        final int startX = 12;
        int y = 48;
        for (int i = 0; i < toasts.size(); i++) {
            Toast t = toasts.get(i);
            TextTexture tex = t.tex;
            renderer.render(tex, startX, y, false, 0, 0, 0, 0);
            y += tex.getHeight() + 6; // vertical spacing
        }
    }

    public void cleanup() {
        if (timeTex != null) timeTex.cleanup();
        for (Toast t : toasts) t.tex.cleanup();
        toasts.clear();
    }
}
