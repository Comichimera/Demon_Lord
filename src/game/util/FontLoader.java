package game.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {
    public static Font loadFont(String path, float size) {
        try {
            InputStream is = FontLoader.class.getResourceAsStream(path);
            if (is == null) {
                throw new IOException("Font file not found: " + path);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Fallback to a default monospaced font if something goes wrong.
            return new Font("Monospaced", Font.PLAIN, (int) size);
        }
    }
}