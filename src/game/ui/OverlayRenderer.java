package game.ui;

import game.core.Window;

import static org.lwjgl.opengl.GL11.*;

public class OverlayRenderer {
    final Window window;

    public OverlayRenderer(Window window) {
        this.window = window;
    }

    public void render(TextTexture tex,
                       int x, int y,
                       boolean fullBg, float bgAlpha,
                       float bgR, float bgG, float bgB)
    {
        int w = window.getWidth(), h = window.getHeight();

        // Set up orthographic projection
        glMatrixMode(GL_PROJECTION);
        glPushMatrix(); glLoadIdentity();
        glOrtho(0, w, h, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix(); glLoadIdentity();

        glDisable(GL_DEPTH_TEST);

        // Full-screen background?
        if (fullBg) {
            glEnable(GL_BLEND);
            glColor4f(bgR, bgG, bgB, bgAlpha);
            glBegin(GL_QUADS);
            glVertex2f(0,  0);
            glVertex2f(w,  0);
            glVertex2f(w,  h);
            glVertex2f(0,  h);
            glEnd();
            glDisable(GL_BLEND);
        }

        // Draw the text texture
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex.getId());
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(1,1,1,1);
        glBegin(GL_QUADS);
        glTexCoord2f(0,0); glVertex2f(x,          y);
        glTexCoord2f(1,0); glVertex2f(x+tex.getWidth(), y);
        glTexCoord2f(1,1); glVertex2f(x+tex.getWidth(), y+tex.getHeight());
        glTexCoord2f(0,1); glVertex2f(x,          y+tex.getHeight());
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        // Restore matrices
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }
}