package game.ui;

import game.core.Window;
import game.inventory.Inventory;
import game.inventory.InventoryEntry;
import game.util.FontLoader;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class InventoryOverlay {

    private final OverlayRenderer renderer;
    private final Window window;
    private final Font font;

    private final Map<String, IconTexture> iconCache = new HashMap<>();

    private static class SlotVisual {
        final TextTexture label;
        final IconTexture icon;

        SlotVisual(TextTexture label, IconTexture icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    private final List<SlotVisual> slots = new ArrayList<>();

    public InventoryOverlay(Window window) {
        this.window = window;
        this.renderer = new OverlayRenderer(window);
        this.font = FontLoader.loadFont("/data/fonts/arial.ttf", 16f);
    }

    public void rebuild(Inventory inventory) {
        for (SlotVisual sv : slots) {
            if (sv.label != null) {
                sv.label.cleanup();
            }
        }
        slots.clear();

        int selectedSlot = inventory.getSelectedSlot();
        List<InventoryEntry> entries = inventory.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            InventoryEntry e = entries.get(i);

            String labelText = String.format(
                    "%d)    %s x%d",
                    i + 1,
                    e.getDefinition().getName(),
                    e.getCount()
            );

            Color labelColor = (i == selectedSlot) ? Color.YELLOW : Color.WHITE;
            TextTexture labelTex = new TextTexture(labelText, font, 4, 2, labelColor);

            IconTexture iconTex = null;
            String iconPath = e.getDefinition().getIconPath();
            if (iconPath != null && !iconPath.isEmpty()) {
                iconTex = loadIcon(iconPath);
            }

            slots.add(new SlotVisual(labelTex, iconTex));
        }
    }

    private IconTexture loadIcon(String path) {
        IconTexture cached = iconCache.get(path);
        if (cached != null) return cached;

        try {
            IconTexture tex = new IconTexture(path);
            iconCache.put(path, tex);
            return tex;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void render() {
        int winW = window.getWidth();
        int winH = window.getHeight();

        int xText = 10;
        int y = winH - 10;

        for (int i = 0; i < slots.size(); i++) {
            SlotVisual sv = slots.get(i);
            TextTexture label = sv.label;
            IconTexture icon = sv.icon;

            if (label == null) continue;

            y -= label.getHeight();
            int labelY = y;

            renderer.render(label, xText, labelY, false, 0, 0, 0, 0);

            if (icon != null) {
                int labelHeight = label.getHeight();

                int iconH = labelHeight - 4;
                if (iconH <= 0) iconH = 16;

                float aspect = (float) icon.getWidth() / (float) icon.getHeight();
                int iconW = Math.round(iconH * aspect);

                int maxIconW = labelHeight * 2;
                if (iconW > maxIconW) {
                    iconW = maxIconW;
                }

                int iconY = labelY + (labelHeight - iconH) / 2;

                int iconX = xText + 28;

                drawIcon(icon, iconX, iconY, iconW, iconH, winW, winH);
            }

            y -= 4;
        }
    }

    private void drawIcon(IconTexture icon,
                          int x, int y, int w, int h,
                          int winW, int winH) {
        if (icon == null) return;

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, winW, winH, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindTexture(GL_TEXTURE_2D, icon.getId());

        int x2 = x + w;
        int y2 = y + h;

        glBegin(GL_QUADS);
        glTexCoord2f(0f, 0f); glVertex2f(x,  y);
        glTexCoord2f(1f, 0f); glVertex2f(x2, y);
        glTexCoord2f(1f, 1f); glVertex2f(x2, y2);
        glTexCoord2f(0f, 1f); glVertex2f(x,  y2);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    public void cleanup() {
        for (SlotVisual sv : slots) {
            if (sv.label != null) {
                sv.label.cleanup();
            }
        }
        slots.clear();

        for (IconTexture tex : iconCache.values()) {
            if (tex != null) {
                tex.cleanup();
            }
        }
        iconCache.clear();
    }
}
