package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;

public class VersionOverlay {
    private final OverlayRenderer renderer;
    private final TextTexture tex;

    public VersionOverlay(Window window, String version) {
        this.renderer = new OverlayRenderer(window);
        Font f = FontLoader.loadFont("/data/fonts/Rubik-Regular.ttf", 14f);
        this.tex = new TextTexture("Version " + version, f, 6, 3, Color.LIGHT_GRAY);
    }

    public void render() {
        renderer.render(tex, 10, renderer.window.getHeight() - tex.getHeight() - 10,
                false, 0,0,0,0);
    }

    public void cleanup() { tex.cleanup(); }
}
