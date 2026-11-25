package game.ui;

public class UiScale {

    public static final float DESIGN_HEIGHT = 720f;
    private static final float STEP = 0.125f;
    private static final float MIN = 0.5f;

    private static float scale = 1f;
    private static int version = 0;
    private static float userScale = 1f;
    private static int lastHeight = (int) DESIGN_HEIGHT;

    private UiScale() {}

    public static void update(int windowHeight) {
        if (windowHeight <= 0) return;
        lastHeight = windowHeight;
        float s = Math.round(windowHeight / DESIGN_HEIGHT / STEP) * STEP;
        s = Math.max(MIN, s);
        if (s != scale) {
            scale = s;
            version++;
        }
    }

    public static void setUserScale(float multiplier) {
        userScale = multiplier;
        update(lastHeight);
    }

    public static float get()               { return scale; }
    public static int   version()           { return version; }

    public static int px(int designPixels)  { return Math.round(designPixels * scale); }
}
