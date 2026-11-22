package game.ui;

import game.core.Window;
import game.util.FontLoader;
import game.world.Player;

import java.awt.*;

public class StatusOverlay {
    private static final Color WHITE  = Color.WHITE;
    private static final Color ORANGE = new Color(255, 165, 0);
    private static final Color RED    = Color.RED;
    private static final int   GAP    = 4;

    private final OverlayRenderer renderer;
    private final Font font;

    private TextTexture damageTex;
    private TextTexture injuryTex;
    private TextTexture bleedTex;

    private String lastDamage = "";
    private String lastInjury = "";
    private String lastBleed  = "";

    public StatusOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/arial.ttf", 16f);
    }

    public void render(Player player) {
        String ds = nvl(player.getDamageStatus());
        String is = nvl(player.getInjuryStatus());
        String bs = nvl(player.getBleedStatus());

        if (!ds.equals(lastDamage)) {
            if (damageTex != null) damageTex.cleanup();
            damageTex = ds.isEmpty() ? null : new TextTexture(ds, font, 10, 5, damageColor(player));
            lastDamage = ds;
        }
        if (!is.equals(lastInjury)) {
            if (injuryTex != null) injuryTex.cleanup();
            injuryTex = is.isEmpty() ? null : new TextTexture(is, font, 10, 5, injuryColor(player));
            lastInjury = is;
        }
        if (!bs.equals(lastBleed)) {
            if (bleedTex != null) bleedTex.cleanup();
            bleedTex = bs.isEmpty() ? null : new TextTexture(bs, font, 10, 5, bleedColor(player));
            lastBleed = bs;
        }

        int winW = renderer.window.getWidth();
        int y = renderer.window.getHeight() - 8;
        if (bleedTex != null) {
            y -= bleedTex.getHeight();
            renderer.render(bleedTex, winW - bleedTex.getWidth() - 8, y, false, 0, 0, 0, 0);
            y -= GAP;
        }
        if (injuryTex != null) {
            y -= injuryTex.getHeight();
            renderer.render(injuryTex, winW - injuryTex.getWidth() - 8, y, false, 0, 0, 0, 0);
            y -= GAP;
        }
        if (damageTex != null) {
            y -= damageTex.getHeight();
            renderer.render(damageTex, winW - damageTex.getWidth() - 8, y, false, 0, 0, 0, 0);
        }
    }

    private Color damageColor(Player p) {
        float d = p.getDamageAcc();
        if (d >= 75f) return RED;
        if (d >= 50f)  return ORANGE;
        return WHITE;
    }

    private Color injuryColor(Player p) {
        float inj = p.getInjuriesAcc();
        if (inj >= 75f) return RED;
        if (inj >= 50f) return ORANGE;
        return WHITE;
    }

    private Color bleedColor(Player p) {
        float rate = p.getBleedRate();
        if (rate >= 2.0f) return RED;
        if (rate >= 0.5f) return ORANGE;
        return WHITE;
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    public void cleanup() {
        if (damageTex != null) { damageTex.cleanup(); damageTex = null; }
        if (injuryTex != null) { injuryTex.cleanup(); injuryTex = null; }
        if (bleedTex  != null) { bleedTex.cleanup();  bleedTex  = null; }
    }
}
