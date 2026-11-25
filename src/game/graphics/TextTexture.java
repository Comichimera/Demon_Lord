package game.graphics;

import game.ui.UiScale;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextTexture {
    private final String text;
    private final Font font;
    private final int padX, padY;
    private final Color color;

    private int textureId, width, height;
    private int builtForVersion = -1;

    public TextTexture(String text, Font font, int padX, int padY, Color color) {
        this.text = text;
        this.font = font;
        this.padX = padX;
        this.padY = padY;
        this.color = color;
        build();
    }

    private void refresh() {
        if (builtForVersion != UiScale.version()) build();
    }

    private void build() {
        if(builtForVersion != -1) glDeleteTextures(textureId);
        builtForVersion = UiScale.version();

        Font scaledFont = font.deriveFont(font.getSize2D() * UiScale.get());
        int px = UiScale.px(padX);
        int py = UiScale.px(padY);

        String[] lines = text.split("\\r?\\n");
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(scaledFont);
        FontMetrics fm = g2.getFontMetrics();
        int maxW = 0, lineH = fm.getHeight();
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));
        g2.dispose();

        width = Math.max(1, maxW + px * 2);
        height = Math.max(1, lineH * lines.length + py * 2);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(scaledFont);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0,0, width, height);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(color);

        int y = py + fm.getAscent();
        for (String l : lines) {
            g.drawString(l, px, y);
            y += lineH;
        }
        g.dispose();

        ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);
        int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        for (int p : pixels) {
            buf.put((byte) ((p >> 16) & 0xff))
                    .put((byte) ((p >> 8) & 0xFF))
                    .put((byte) (p & 0xFF))
                    .put((byte) ((p >> 24) & 0xFF));
        }
        buf.flip();

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getId()      { refresh(); return textureId;  }
    public int getWidth()   { refresh(); return width;      }
    public int getHeight()  { refresh(); return height;     }

    public void cleanup()   { glDeleteTextures(textureId);  }
}