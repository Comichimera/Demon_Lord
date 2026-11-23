package game.util;

import game.config.GameConfig;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

    public static Font regular(float size) { return loadFont(GameConfig.path("font.regular", "/data/fonts/arial.ttf"), size); }
    public static Font bold(float size) { return loadFont(GameConfig.path("font.bold", "/data/fonts/arial-bold.ttf"), size); }
    public static Font menu(float size) { return loadFont(GameConfig.path("font.menu", "/data/fonts/Rubik-Regular.ttf"), size); }
    public static Font title(float size) { return loadFont(GameConfig.path("font.title", "/data/fonts/rubik-BoldItalic.ttf"), size); }

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
            return new Font("Monospaced", Font.PLAIN, (int) size);
        }
    }
}