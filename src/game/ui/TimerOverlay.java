package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;

public class TimerOverlay {
    private final OverlayRenderer renderer;
    private final Font font;
    private TextTexture tex;
    private String last = "";

    public TimerOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/RubikMonoOne.ttf", 20f);
        this.last     = "";
    }

    public String formatTime(double elapsedTime) {
        int minutes = (int)(elapsedTime / 60);
        double seconds = elapsedTime - minutes * 60; // ensures seconds < 60 normally
        // Clamp seconds to avoid it displaying as 60.0.
        if (seconds >= 59.95) {
            seconds = 0;
            minutes++;
        }
        return String.format("%d:%04.1f", minutes, seconds);
    }

    public void update(double elapsed) {
        String s = formatTime(elapsed); // same as before
        if (!s.equals(last)) {
            if (tex != null) tex.cleanup();
            tex = new TextTexture(s, font, 10, 5, Color.WHITE);
            last = s;
        }
    }

    public void render() {
        int x = renderer.window.getWidth() - tex.getWidth() - 10;
        int y = 10;
        renderer.render(tex, x, y, false, 0,0,0,0);
    }

    public void cleanup() {
        if (tex != null) tex.cleanup();
    }
}
