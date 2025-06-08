package game.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

public class TextTexture {
    private final int textureId, width, height;

    public TextTexture(String text, Font font, int padX, int padY, Color color) {
        // 1) Measure text
        String[] lines = text.split("\\r?\\n");
        BufferedImage tmp = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int maxW = 0, lineH = fm.getHeight();
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));
        g2.dispose();

        width  = maxW + padX*2;
        height = lineH*lines.length + padY*2;

        // 2) Paint text into image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0,0,width,height);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(color);

        int y = padY + fm.getAscent();
        for (String l : lines) {
            g.drawString(l, padX, y);
            y += lineH;
        }
        g.dispose();

        // 3) Copy pixels into ByteBuffer
        ByteBuffer buf = BufferUtils.createByteBuffer(width*height*4);
        int[] pixels = img.getRGB(0,0,width,height,null,0,width);
        for (int p : pixels) {
            buf.put((byte)((p>>16)&0xFF))
                    .put((byte)((p>>8)&0xFF))
                    .put((byte)(p&0xFF))
                    .put((byte)((p>>24)&0xFF));
        }
        buf.flip();

        // 4) Upload to OpenGL texture
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getId()    { return textureId; }
    public int getWidth(){ return width;     }
    public int getHeight(){ return height;    }

    public void cleanup() { glDeleteTextures(textureId); }
}