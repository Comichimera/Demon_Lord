package game.debug;

import game.config.GameConfig;
import game.core.Window;
import game.map.MapData;
import game.map.Tile;
import game.ui.OverlayRenderer;
import game.ui.TextTexture;
import game.util.FontLoader;
import game.world.Player;
import game.world.enemy.EnemyManager;

import java.awt.Color;
import java.awt.Font;

public class DebugOverlay {
    private static final Color COLOR = new Color(0f, 1f, 0f, 1f);

    private final OverlayRenderer renderer;
    private final Font font;
    private TextTexture tex;
    private String lastText = "";

    public DebugOverlay(Window window) {
        this.renderer = new OverlayRenderer(window);
        this.font     = FontLoader.loadFont("/data/fonts/arial.ttf", 13f);
    }

    public void update(Player player, EnemyManager enemies, MapData map, float fps, float dt) {
        if (!DebugConfig.overlayVisible) return;

        int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
        int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);

        StringBuilder sb = new StringBuilder("[DEBUG]\n");
        for (DebugValue v : DebugValue.values()) {
            if (!DebugConfig.enabled.contains(v)) continue;
            switch (v) {
                case POSITION ->
                    sb.append(String.format("Pos (world): %.2f / %.2f%n", player.getX(), player.getZ()));
                case YAW ->
                    sb.append(String.format("Yaw: %.1f°%n", player.getYaw()));
                case GRID_TILE ->
                    sb.append(String.format("Tile: %d, %d%n", tx, ty));
                case SPEED_MULT ->
                    sb.append(String.format("Speed: %.2fx%n", player.getSpeedMultiplier()));
                case ATTACK_SPEED_MULT ->
                    sb.append(String.format("Atk speed: %.2fx%n", player.getAttackSpeedMultiplier()));
                case DAMAGE_MULT ->
                    sb.append(String.format("Damage mult: %.2fx%n", player.getDamageMultiplier()));
                case HEALTH_STATUS -> {
                    String d = player.getDamageStatus(), inj = player.getInjuryStatus(), b = player.getBleedStatus();
                    sb.append("Dmg:    ").append(d   != null ? d   : "Fine").append('\n');
                    sb.append("Injury: ").append(inj != null ? inj : "None").append('\n');
                    sb.append("Bleed:  ").append(b   != null ? b   : "None").append('\n');
                }
                case DAMAGE_ACC -> {
                    float d = player.getDamageAcc();
                    if (d >= 0) sb.append(String.format("Damage acc: %.1f%n", d));
                }
                case INJURIES_ACC -> {
                    float inj = player.getInjuriesAcc();
                    if (inj >= 0) sb.append(String.format("Injuries: %.1f%n", inj));
                }
                case BLOODLOSS -> {
                    float bl = player.getBloodloss();
                    if (bl >= 0) sb.append(String.format("Bloodloss: %.1f / 100%n", bl));
                }
                case BLEED_RATE ->
                    sb.append(String.format("Bleed: %.4f /s%n", player.getBleedRate()));
                case HP_CLASSIC -> {
                    if (player.getHp() >= 0)
                        sb.append(String.format("HP: %d / %d%n", player.getHp(), player.getMaxHp()));
                }
                case ENEMY_COUNT ->
                    sb.append("Enemies: ").append(enemies.getAliveCount()).append('\n');
                case NEAREST_ENEMY -> {
                    float d = enemies.nearestEnemyDistanceTiles(player);
                    sb.append(d < 0 ? "Nearest: N/A\n" : String.format("Nearest: %.1f tiles%n", d));
                }
                case PLAYER_SPOTTED ->
                    sb.append("Spotted: ").append(enemies.isPlayerSpotted() ? "YES" : "NO").append('\n');
                case FACING_TILE -> {
                    double rad = Math.toRadians(player.getYaw());
                    int fx = (int)((player.getX() + Math.sin(rad) * GameConfig.TILE_SIZE) / GameConfig.TILE_SIZE);
                    int fy = (int)((player.getZ() - Math.cos(rad) * GameConfig.TILE_SIZE) / GameConfig.TILE_SIZE);
                    Tile t = (fy >= 0 && fy < map.getHeight() && fx >= 0 && fx < map.getWidth())
                            ? map.getTile(fy, fx) : null;
                    if (t == null) sb.append("Facing: OOB\n");
                    else sb.append(String.format("Facing: %c%s%s%n",
                            t.getType(),
                            t.isWalkable() ? " walk" : "",
                            t.isLocked() ? " locked" : (t.isOpenable() ? " openable" : "")));
                }
                case FPS ->
                    sb.append(String.format("FPS: %.0f%n", fps));
                case FRAME_TIME ->
                    sb.append(String.format("Frame: %.1fms%n", dt * 1000f));
            }
        }

        String text = sb.toString();
        if (!text.equals(lastText)) {
            if (tex != null) tex.cleanup();
            tex = new TextTexture(text, font, 8, 4, COLOR);
            lastText = text;
        }
    }

    public void render() {
        if (!DebugConfig.overlayVisible || tex == null) return;
        renderer.render(tex, 8, 8, true, 0.55f, 0f, 0f, 0f);
    }

    public void cleanup() {
        if (tex != null) { tex.cleanup(); tex = null; }
        lastText = "";
    }
}
