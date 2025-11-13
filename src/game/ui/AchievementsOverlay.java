package game.ui;

import game.core.Window;
import game.util.FontLoader;
import game.achievements.Achievement;
import game.achievements.AchievementsManager;
import game.graphics.TextureLoader;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class AchievementsOverlay {

    private final OverlayRenderer renderer;

    private TextTexture headerTex;
    private final List<Entry> entries = new ArrayList<>();

    private static final int ICON_SIZE = 48;
    private static final int ROW_GAP   = 10;

    private static final class Entry {
        final Achievement achievement;
        final TextTexture text;
        final int lockedTex;
        final int unlockedTex;

        Entry(Achievement achievement, TextTexture text, int lockedTex, int unlockedTex) {
            this.achievement = achievement;
            this.text = text;
            this.lockedTex = lockedTex;
            this.unlockedTex = unlockedTex;
        }

        void cleanup() {
            if (text != null) text.cleanup();
            if (lockedTex != 0) glDeleteTextures(lockedTex);
            if (unlockedTex != 0 && unlockedTex != lockedTex) glDeleteTextures(unlockedTex);
        }
    }

    public AchievementsOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);

        Font headerFont = FontLoader.loadFont("/data/font.ttf", 28f);
        Font bodyFont   = FontLoader.loadFont("/data/font.ttf", 18f);

        headerTex = new TextTexture("Achievements", headerFont, 4, 4, Color.WHITE);

        for (Achievement a : AchievementsManager.get().list()) {
            String lines = a.title + "\n" + a.description;

            Color color = a.unlocked ? Color.WHITE : new Color(200, 200, 200, 255);
            TextTexture text = new TextTexture(lines, bodyFont, 4, 2, color);

            String[] paths = resolveIconPaths(a);
            int locked = loadIcon(paths[0]);
            int unlocked = loadIcon(paths[1]);
            if (locked == 0 && unlocked != 0) locked = unlocked;
            if (unlocked == 0 && locked != 0) unlocked = locked;

            entries.add(new Entry(a, text, locked, unlocked));
        }
    }

    public void render() {
        int x = 40;
        int y = 40;

        renderer.render(headerTex, x, y, false, 0, 0, 0, 0);
        y += headerTex.getHeight() + 16;

        for (Entry e : entries) {
            int iconTex = e.achievement.unlocked ? e.unlockedTex : e.lockedTex;
            if (iconTex != 0) {
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                glMatrixMode(GL_PROJECTION);
                glPushMatrix();
                glLoadIdentity();
                glOrtho(0, renderer.window.getWidth(), renderer.window.getHeight(), 0, -1, 1);

                glMatrixMode(GL_MODELVIEW);
                glPushMatrix();
                glLoadIdentity();

                glBindTexture(GL_TEXTURE_2D, iconTex);
                glColor3f(1, 1, 1);
                glBegin(GL_QUADS);
                glTexCoord2f(0, 0); glVertex2f(x,              y);
                glTexCoord2f(1, 0); glVertex2f(x + ICON_SIZE,  y);
                glTexCoord2f(1, 1); glVertex2f(x + ICON_SIZE,  y + ICON_SIZE);
                glTexCoord2f(0, 1); glVertex2f(x,              y + ICON_SIZE);
                glEnd();

                glDisable(GL_TEXTURE_2D);
                glDisable(GL_BLEND);
                glEnable(GL_DEPTH_TEST);

                glMatrixMode(GL_PROJECTION);
                glPopMatrix();
                glMatrixMode(GL_MODELVIEW);
                glPopMatrix();
            }

            int textX = x + ICON_SIZE + 10;
            renderer.render(e.text, textX, y, false, 0, 0, 0, 0);

            y += Math.max(ICON_SIZE, e.text.getHeight()) + ROW_GAP;
        }
    }

    private String[] resolveIconPaths(Achievement a) {
        String locked = null, unlocked = null;
        try {
            Field fLocked = a.getClass().getField("lockedIconPath");
            Object lp = fLocked.get(a);
            if (lp != null) locked = lp.toString();
        } catch (Throwable ignore) {}
        try {
            Field fUnlocked = a.getClass().getField("unlockedIconPath");
            Object up = fUnlocked.get(a);
            if (up != null) unlocked = up.toString();
        } catch (Throwable ignore) {}
        if (locked == null && unlocked == null) {
            try {
                Field f = a.getClass().getField("iconPath");
                Object v = f.get(a);
                String p = (v != null) ? v.toString() : null;
                return new String[] { p, p };
            } catch (Throwable ignore) {
                return new String[] { null, null };
            }
        }
        if (locked == null) locked = unlocked;
        if (unlocked == null) unlocked = locked;
        return new String[] { locked, unlocked };
    }

    public void cleanup() {
        if (headerTex != null) headerTex.cleanup();
        for (Entry e : entries) e.cleanup();
    }

    public static int loadIcon(String path) {
        if (path == null || path.isEmpty()) return 0;
        try {
            String p = (path.startsWith("/")) ? path : ("/" + path);
            return TextureLoader.loadTexture(p);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
