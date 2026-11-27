package game.core.state;

import game.audio.AudioSystem;
import game.config.Settings;
import game.core.Game;
import game.core.IGameState;
import game.core.Window;
import game.graphics.TextTexture;
import game.input.EAction;
import game.input.Input;
import game.ui.OverlayRenderer;
import game.ui.UiScale;
import game.util.FontLoader;

import java.awt.Color;
import java.awt.Font;

public class SettingsState implements IGameState {
    private static final String[] LABELS = {
            "Master volume", "Music volume", "Effects volume", "Fullscreen", "Window size", "Vertical Sync", "Field of view", "Turn speed", "UI size"
    };

    private static final Color FADED = new Color(1f, 1f, 1f, 0.6f);

    private final Game game;
    private final OverlayRenderer renderer;
    private final Font font;
    private TextTexture titleTex, markerTex, labelsTex, valuesTex, helpTex;
    private int selected = 0;

    public SettingsState(Game game) {
        this.game = game;
        this.renderer = new OverlayRenderer(game.window);
        this.font = FontLoader.menu(24f);
    }

    public static void applyAll(Window window) {
        AudioSystem.setMasterVolume(Settings.masterVolume);
        AudioSystem.setMusicVolume(Settings.musicVolume);
        AudioSystem.setSfxVolume(Settings.sfxVolume);
        window.setVsync(Settings.vsync);
        window.applyWindowMode();
        UiScale.setUserScale(Settings.uiSize);
    }

    @Override
    public void enter() {
        titleTex = new TextTexture("Settings", FontLoader.menu(32f), 10, 5, Color.WHITE);
        helpTex = new TextTexture("Up/Down: choose      Left/Right: change      Esc: save and go back", FontLoader.menu(14f), 10, 5, FADED);
        rebuild();

    }

    @Override
    public void update(float dt) {

        if (Input.pressed(EAction.BACK)) {
            Settings.save();
            game.popState();
            return;
        }

        boolean changed = false;
        if (Input.pressed(EAction.MENU_UP)) { selected = (selected + LABELS.length - 1) % LABELS.length; changed = true; }
        if (Input.pressed(EAction.MENU_DOWN)) { selected = (selected + 1) % LABELS.length; changed = true; }
        if (Input.pressed(EAction.MENU_LEFT)) { change(-1); changed = true; }
        if (Input.pressed(EAction.MENU_RIGHT)) { change(+1); changed = true; }
        if (changed) rebuild();
    }

    private void change(int dir) {
        switch (selected) {
            case 0 -> {
                Settings.masterVolume = step(Settings.masterVolume, dir * 0.1f, 0f, 1f); AudioSystem.setMasterVolume(Settings.masterVolume);
            }
            case 1 -> {
                Settings.musicVolume = step(Settings.musicVolume, dir * 0.1f, 0f, 1f); AudioSystem.setMusicVolume(Settings.musicVolume);
            }
            case 2 -> {
                Settings.sfxVolume = step(Settings.sfxVolume, dir * 0.1f, 0f, 1f); AudioSystem.setSfxVolume(Settings.sfxVolume);
            }
            case 3 -> {
                Settings.fullscreen = !Settings.fullscreen; game.window.applyWindowMode();
            }
            case 4 -> {
                Settings.windowSize = Math.floorMod(Settings.windowSize + dir, Settings.WINDOW_SIZES.length);
                if(!Settings.fullscreen) game.window.applyWindowMode();
            }
            case 5 -> {
                Settings.vsync = !Settings.vsync; game.window.setVsync(Settings.vsync);
            }
            case 6 -> {
                Settings.fov = Math.max(50, Math.min(90, Settings.fov + dir * 5));
            }
            case 7 -> {
                Settings.turnSpeed = Math.max(45, Math.min(240, Settings.turnSpeed + dir * 15));
            }
            case 8 -> {
                Settings.uiSize = step(Settings.uiSize, dir * 0.25f, 0.75f, 1.5f); UiScale.setUserScale(Settings.uiSize);
            }
        }


    }

    private static float step(float value, float delta, float min, float max) {
        float v = Math.round((value + delta) * 100f) / 100f;
        return Math.max(min, Math.min(max, v));
    }

    private static String percent(float v) {
        return Math.round(v * 100) + "%";
    }

    private String valueText(int i) {
        int[] size = Settings.WINDOW_SIZES[Settings.windowSize];
        return switch (i) {
            case 0 -> percent(Settings.masterVolume);
            case 1 -> percent(Settings.musicVolume);
            case 2 -> percent(Settings.sfxVolume);
            case 3 -> Settings.fullscreen ? "On" : "Off";
            case 4 -> size[0] + " x " + size[1] + (Settings.fullscreen ? " (windowed only)" : "");
            case 5 -> Settings.vsync ? "On" : "Off";
            case 6 -> Settings.fov == 90 ? "Doom Pro" : Settings.fov + "°";
            case 7 -> Settings.turnSpeed + "°/s";
            case 8 -> percent(Settings.uiSize);
            default -> "";
        };
    }

    private void rebuild() {
        StringBuilder marker = new StringBuilder(), labels = new StringBuilder(), values = new StringBuilder();
        for (int i = 0; i < LABELS.length; i++) {
            boolean sel = i == selected;
            marker.append(sel ? ">" : " ");
            labels.append(LABELS[i]);
            values.append(sel ? "< " : "    ").append(valueText(i)).append(sel ? " >" : "");
            if (i < LABELS.length - 1) {
                marker.append('\n');
                labels.append('\n'); values.append('\n');
            }

        }
        if (markerTex != null) markerTex.cleanup();
        if (labelsTex != null) labelsTex.cleanup();
        if (valuesTex != null) valuesTex.cleanup();
        markerTex = new TextTexture(marker.toString(), font, 10, 5, Color.WHITE);
        labelsTex = new TextTexture(labels.toString(), font, 10, 5, Color.WHITE);
        valuesTex = new TextTexture(values.toString(), font, 10, 5, Color.WHITE);
    }

    @Override
    public void render() {
        int x = UiScale.px(20);
        int y = UiScale.menuTop();
        renderer.render(titleTex, x, y, true, 0.75f, 0f, 0f, 0f);
        y += titleTex.getHeight() + UiScale.px(10);
        int labelsX = x + UiScale.px(24);
        renderer.render(markerTex, x, y, false, 0f, 0f, 0f, 0f);
        renderer.render(labelsTex, labelsX, y, false, 0f, 0f, 0f, 0f);
        renderer.render(valuesTex, labelsX + labelsTex.getWidth() + UiScale.px(40), y, false, 0f, 0f, 0f, 0f);
        y += labelsTex.getHeight() + UiScale.px(10);
        renderer.render(helpTex, x, y, false, 0f, 0f, 0f, 0f);
    }

    @Override
    public void exit() {
        if (titleTex != null) titleTex.cleanup();
        if (markerTex != null) markerTex.cleanup();
        if (labelsTex != null) labelsTex.cleanup();
        if (valuesTex != null) valuesTex.cleanup();
        if (helpTex != null) helpTex.cleanup();
    }
}
