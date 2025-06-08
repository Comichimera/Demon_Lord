package game.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import game.util.FontLoader;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

public class MainMenuOverlay {
    private int textureId;
    private int width, height;
    private String text;
    private Font font;

    public MainMenuOverlay(String text) {
        this.text = text;
        font = FontLoader.loadFont("/data/fonts/Rubik-Regular.ttf", 24f);
        generateTexture();
    }

    private void generateTexture() {
        String[] lines = text.split("\\r?\\n");
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImg.createGraphics();
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        int maxWidth = 0;
        for (String line : lines) {
            int lineWidth = metrics.stringWidth(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }
        int lineHeight = metrics.getHeight();
        int totalHeight = lineHeight * lines.length;
        g2d.dispose();

        int paddingX = 20;
        int paddingY = 20;
        int imageWidth = maxWidth + paddingX * 2;
        int imageHeight = totalHeight + paddingY * 2;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Color.WHITE);

        int y = paddingY + metrics.getAscent();
        for (String line : lines) {
            g.drawString(line, paddingX, y);
            y += lineHeight;
        }
        g.dispose();

        width = imageWidth;
        height = imageHeight;

        int[] pixels = new int[imageWidth * imageHeight];
        image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
        ByteBuffer buffer = BufferUtils.createByteBuffer(imageWidth * imageHeight * 4);
        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                int pixel = pixels[j * imageWidth + i];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, imageWidth, imageHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render() {
        // Set up an orthographic projection for a full-screen overlay.
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glColor3f(0, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(800, 0);
        glVertex2f(800, 600);
        glVertex2f(0, 600);
        glEnd();

        // Draw the menu text centered.
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f((800 - width) / 2, (600 - height) / 2);
        glTexCoord2f(1, 0); glVertex2f((800 + width) / 2, (600 - height) / 2);
        glTexCoord2f(1, 1); glVertex2f((800 + width) / 2, (600 + height) / 2);
        glTexCoord2f(0, 1); glVertex2f((800 - width) / 2, (600 + height) / 2);
        glEnd();
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }
}
