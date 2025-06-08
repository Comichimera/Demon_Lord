package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;

public class LevelCompleteOverlay {
    private final OverlayRenderer renderer;
    private final TextTexture tex;

    public LevelCompleteOverlay(Window window, String text) {
        this.renderer = new OverlayRenderer(window);
        Font f = FontLoader.loadFont("/data/fonts/Rubik-BoldItalic.ttf", 32f);
        this.tex = new TextTexture(text, f, 20, 10, Color.YELLOW);
    }

    public void render() {
        int x = (renderer.window.getWidth()  - tex.getWidth())  / 2;
        int y = (renderer.window.getHeight() - tex.getHeight()) / 2;
        renderer.render(tex, x, y, true, 0.5f, 0f,0f,0f);
    }

    public void cleanup() { tex.cleanup(); }
}
