package game.core.state;

import game.core.Game;
import game.core.IGameState;
import game.debug.DebugConfig;
import game.debug.EDebugValue;
import game.input.EAction;
import game.input.Input;
import game.ui.OverlayRenderer;
import game.graphics.TextTexture;
import game.util.FontLoader;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class DebugMenuState implements IGameState {

    private static final EDebugValue[] VALUES = EDebugValue.values();
    private static final int IDX_GAMEPLAY   = VALUES.length;
    private static final int IDX_PREVIEW    = VALUES.length + 1;
    private static final int ITEM_COUNT     = VALUES.length + 2;

    private static final Color GREEN = new Color(0f, 1f, 0f, 1f);

    private final Game game;
    private final OverlayRenderer renderer;
    private final Font font;

    private TextTexture menuTex;
    private TextTexture previewTex;
    private String lastMenuText = "";

    private int     selected    = 0;
    private boolean showPreview = false;

    public DebugMenuState(Game game) {
        this.game     = game;
        this.renderer = new OverlayRenderer(game.window);
        this.font     = FontLoader.regular( 14f);
    }

    @Override
    public void enter() {
        rebuild();
    }

    @Override
    public void update(float dt) {
        boolean changed = false;

        if (Input.pressed(EAction.MENU_UP)) {
            selected = (selected + ITEM_COUNT - 1) % ITEM_COUNT;
            changed = true;
        }

        if (Input.pressed(EAction.MENU_UP)) {
            selected = (selected + 1) % ITEM_COUNT;
            changed = true;
        }

        if (Input.pressed(EAction.CONFIRM)) {
            activate();
            changed = true;
        }

        if (Input.pressed(EAction.BACK)) {
            game.changeState(new MainMenuState(game));
            return;
        }

        if (changed) rebuild();
    }

    private void activate() {
        if (selected < VALUES.length) {
            EDebugValue v = VALUES[selected];
            if (DebugConfig.enabled.contains(v)) DebugConfig.enabled.remove(v);
            else DebugConfig.enabled.add(v);
        } else if (selected == IDX_GAMEPLAY) {
            DebugConfig.gameplayTogglable = !DebugConfig.gameplayTogglable;
            if (!DebugConfig.gameplayTogglable) DebugConfig.overlayVisible = false;
        } else if (selected == IDX_PREVIEW) {
            showPreview = !showPreview;
            if (!showPreview && previewTex != null) { previewTex.cleanup(); previewTex = null; }
        }
    }

    private void rebuild() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBUG MENU ===\n");
        sb.append("UP/DOWN  navigate    SPACE  toggle    ESC  exit\n\n");

        for (int i = 0; i < VALUES.length; i++) {
            sb.append(i == selected ? "> " : "  ");
            sb.append(DebugConfig.enabled.contains(VALUES[i]) ? "[X] " : "[ ] ");
            sb.append(VALUES[i].label).append('\n');
        }

        sb.append('\n');
        sb.append(selected == IDX_GAMEPLAY ? "> " : "  ");
        sb.append(DebugConfig.gameplayTogglable ? "[X] " : "[ ] ");
        sb.append("Gameplay Toggleable\n");

        sb.append(selected == IDX_PREVIEW ? "> " : "  ");
        sb.append(showPreview ? "[ON] " : "     ");
        sb.append("Preview");

        String text = sb.toString();
        if (text.equals(lastMenuText)) return;
        if (menuTex != null) menuTex.cleanup();
        menuTex = new TextTexture(text, font, 8, 4, Color.WHITE);
        lastMenuText = text;

        if (showPreview) rebuildPreview();
    }

    private void rebuildPreview() {
        if (previewTex != null) { previewTex.cleanup(); previewTex = null; }
        StringBuilder sb = new StringBuilder("[DEBUG]\n");
        for (EDebugValue v : VALUES) {
            if (!DebugConfig.enabled.contains(v)) continue;
            sb.append(switch (v) {
                case POSITION          -> "Pos (world): 7.50 / 4.50\n";
                case YAW               -> "Yaw: 180.0°\n";
                case GRID_TILE         -> "Tile: 2, 1\n";
                case SPEED_MULT        -> "Speed: 1.00x\n";
                case ATTACK_SPEED_MULT -> "Atk speed: 1.00x\n";
                case DAMAGE_MULT       -> "Damage mult: 1.00x\n";
                case HEALTH_STATUS     -> "Status: You are bruised\n";
                case DAMAGE_ACC        -> "Damage acc: 23.5\n";
                case INJURIES_ACC      -> "Injuries: 6.0\n";
                case BLOODLOSS         -> "Bloodloss: 0.3 / 100\n";
                case BLEED_RATE        -> "Bleed: 0.0003 /s\n";
                case HP_CLASSIC        -> "HP: 80 / 100\n";
                case ENEMY_COUNT       -> "Enemies: 2\n";
                case NEAREST_ENEMY     -> "Nearest: 3.2 tiles\n";
                case PLAYER_SPOTTED    -> "Spotted: NO\n";
                case FACING_TILE       -> "Facing: F walk\n";
                case FPS               -> "FPS: 60\n";
                case FRAME_TIME        -> "Frame: 16.7ms\n";
            });
        }
        if (sb.length() > "[DEBUG]\n".length()) {
            previewTex = new TextTexture(sb.toString(), font, 8, 4, GREEN);
        }
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glClearColor(0.05f, 0.05f, 0.1f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (menuTex != null) {
            renderer.render(menuTex, 20, 20, false, 0f, 0f, 0f, 0f);
        }
        if (showPreview && previewTex != null) {
            int x = game.window.getWidth() - previewTex.getWidth() - 20;
            renderer.render(previewTex, x, 20, true, 0.55f, 0f, 0f, 0f);
        }
    }

    @Override
    public void exit() {
        if (menuTex    != null) { menuTex.cleanup();    menuTex    = null; }
        if (previewTex != null) { previewTex.cleanup(); previewTex = null; }
        lastMenuText = "";
    }
}
