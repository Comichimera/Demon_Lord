package game.ui;

import game.core.Window;
import game.util.FontLoader;
import game.world.Player;

import java.awt.*;

public class HpOverlay {
    private final OverlayRenderer renderer;
    private final Font font;
    private TextTexture hpTex;
    private int lastHp = -1;

    public HpOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/arial.ttf", 16f);
    }

    public void render(Player player) {
        int hp    = player.getHp();
        int maxHp = player.getMaxHp();

        if (hp != lastHp) {
            if (hpTex != null) hpTex.cleanup();
            Color col = (hp > maxHp * 0.3f) ? Color.WHITE : Color.RED;
            hpTex  = new TextTexture("HP: " + hp + " / " + maxHp, font, 10, 5, col);
            lastHp = hp;
        }

        if (hpTex != null) {
            int y = renderer.window.getHeight() - hpTex.getHeight() - 8;
            renderer.render(hpTex, 8, y, false, 0, 0, 0, 0);
        }
    }

    public void cleanup() {
        if (hpTex != null) { hpTex.cleanup(); hpTex = null; }
    }
}
