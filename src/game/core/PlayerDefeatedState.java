package game.core;

import game.ui.TextTexture;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PlayerDefeatedState implements IGameState {

    private final Game game;
    private TextTexture youDiedTexture;
    private float elapsedSeconds = 0f;
    private boolean enterPressed = false;

    // Animation tuning
    private static final float START_SCALE   = 0.9f;  // starting size
    private static final float END_SCALE     = 1.2f;  // final size
    private static final float GROW_DURATION = 1.5f;  // seconds

    public PlayerDefeatedState(Game game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Font font = FontLoader.loadFont("/fonts/RubikMonoOne.ttf", 72f);

        youDiedTexture = new TextTexture(
                "YOU DIEDED",
                font,
                16, 16,
                new Color(190, 0, 0)
        );

        elapsedSeconds = 0f;
        enterPressed = false;
    }

    @Override
    public void update(float dt) {
        elapsedSeconds += dt;

        long handle = game.window.getWindowHandle();
        int state = glfwGetKey(handle, GLFW_KEY_ENTER);

        if (state == GLFW_PRESS && !enterPressed) {
            enterPressed = true;

            // Restart the current level
            game.changeState(new PlayingState(game));
        } else if (state == GLFW_RELEASE) {
            enterPressed = false;
        }
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (youDiedTexture == null) {
            return;
        }

        int winW = game.window.getWidth();
        int winH = game.window.getHeight();

        float t = elapsedSeconds / GROW_DURATION;
        if (t > 1f) t = 1f;

        float eased = t * t * (3f - 2f * t);

        float scale = START_SCALE + (END_SCALE - START_SCALE) * eased;

        float texW = youDiedTexture.getWidth() * scale;
        float texH = youDiedTexture.getHeight() * scale;

        float xPix = (winW - texW) * 0.5f;
        float yPix = (winH - texH) * 0.5f;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float left   =  2.0f * xPix / winW - 1.0f;
        float right  =  2.0f * (xPix + texW) / winW - 1.0f;
        float top    =  1.0f - 2.0f * yPix / winH;
        float bottom =  1.0f - 2.0f * (yPix + texH) / winH;

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, youDiedTexture.getId());

        glBegin(GL_QUADS);
        glTexCoord2f(0f, 0f); glVertex2f(left,  top);
        glTexCoord2f(1f, 0f); glVertex2f(right, top);
        glTexCoord2f(1f, 1f); glVertex2f(right, bottom);
        glTexCoord2f(0f, 1f); glVertex2f(left,  bottom);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    @Override
    public void exit() {
        if (youDiedTexture != null) {
            youDiedTexture.cleanup();
            youDiedTexture = null;
        }
    }
}
