package game.ui;

import game.core.Window;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;

public class VersionOverlay {
    private final OverlayRenderer renderer;
    private final Font font;
    private TextTexture tex;
    private String lastRendered = "";
    private String lastVersion  = "";
    private final int margin = 10;

    public VersionOverlay(Window window, String version) {
        this.renderer = new OverlayRenderer(window);
        // Use same family/size philosophy as your Timer (crisper at slightly larger sizes)
        this.font = FontLoader.loadFont("/data/fonts/RubikMonoOne.ttf", 16f);
        setVersion(version);
    }

    /** Call whenever version might change (or once from ctor). */
    public void setVersion(String version) {
        if (version == null) version = "";
        if (version.equals(lastVersion) && tex != null) return;

        String os = System.getProperty("os.name").toLowerCase();
        String suffix;

        if (os.contains("win")) {
            suffix = " (win)";
        } else if (os.contains("mac")) {
            suffix = " (mac)";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            suffix = " (dumb linuxoid version)";
        } else {
            suffix = "";
        }

        String raw = version + suffix;
        int maxW = renderer.window.getWidth() - margin * 2;

        // Ellipsize if too wide
        String fitted = fitToWidth(font, raw, maxW);

        // Rebuild only if the exact drawn text changed
        if (!fitted.equals(lastRendered)) {
            if (tex != null) tex.cleanup();
            // Slightly higher contrast & alpha helps perceived sharpness
            Color c = new Color(255, 255, 255, 220);
            tex = new TextTexture(fitted, font, 6, 3, c);
            lastRendered = fitted;
        }
        lastVersion = version;
    }

    public void render() {
        if (tex == null) return;
        // Bottom-right, snapped to integer pixels
        int x = 10;
        int y = 10;
        renderer.render(tex, x, y, false, 0, 0, 0, 0);
    }

    public void cleanup() { if (tex != null) tex.cleanup(); }

    // --- helpers ---

    private static String fitToWidth(Font font, String s, int maxW) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        if (textWidth(font, s, frc) <= maxW) return s;

        final String ell = "…";
        int lo = 0, hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            String cand = s.substring(0, mid) + ell;
            if (textWidth(font, cand, frc) <= maxW) lo = mid + 1; else hi = mid;
        }
        int take = Math.max(0, lo - 1);
        return s.substring(0, take) + ell;
    }

    private static int textWidth(Font f, String s, FontRenderContext frc) {
        return (int)Math.ceil(f.getStringBounds(s, frc).getWidth());
    }
}