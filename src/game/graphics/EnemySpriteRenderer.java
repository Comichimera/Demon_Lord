package game.graphics;

import game.config.GameConfig;
import game.world.Player;
import game.world.enemy.Enemy;
import game.world.enemy.EnemyManager;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

public final class EnemySpriteRenderer {

    private static final class QuadSet {
        final TexturedQuadRenderer front, side, back;
        final float heightMeters;

        QuadSet(TexturedQuadRenderer f, TexturedQuadRenderer s, TexturedQuadRenderer b, float hMeters) {
            this.front = f; this.side = s; this.back = b; this.heightMeters = hMeters;
        }

        void cleanup() {
            front.cleanup();
            side.cleanup();
            back.cleanup();
        }
    }

    private final SpriteDefsLoader defs;
    private final Map<String, QuadSet> cache = new HashMap<>();

    public EnemySpriteRenderer(SpriteDefsLoader defs) {
        this.defs = defs;
    }

    public void render(EnemyManager manager, Player cameraPlayer, Matrix4f projection, Matrix4f view) {
        if (manager == null) return;

        final float camYaw = cameraPlayer.getYaw();

        for (Enemy e : manager.getEnemies()) {
            QuadSet qs = cache.computeIfAbsent(e.getType().toLowerCase(), this::buildQuadSetForType);

            // choose texture (front/side/back) by enemy yaw vs camera yaw
            float delta = normalizeDeg(e.getYaw() - camYaw);
            TexturedQuadRenderer chosen = selectRenderer(qs, delta);

            // model: position at (x, z), feet on floor (y = height/2), face camera (−camYaw), stand up (+90° X)
            Matrix4f model = new Matrix4f()
                    .translation(e.getX(), qs.heightMeters * 0.5f, e.getZ())
                    .rotateY((float) toRadians(-camYaw))
                    .rotateX((float) toRadians(90.0));

            chosen.render(projection, view, model);
        }
        System.out.println("done");
    }

    public void cleanup() {
        for (QuadSet qs : cache.values()) qs.cleanup();
        cache.clear();
    }

    // --- helpers ---

    private QuadSet buildQuadSetForType(String type) {
        SpriteDefsLoader.SpriteDef d = defs.get(type);
        float w = d.widthTiles  * GameConfig.TILE_SIZE;
        float h = d.heightTiles * GameConfig.TILE_SIZE;
        try {
            TexturedQuadRenderer f = new TexturedQuadRenderer(d.frontPath, w, h);
            TexturedQuadRenderer s = new TexturedQuadRenderer(d.sidePath,  w, h);
            TexturedQuadRenderer b = new TexturedQuadRenderer(d.backPath,  w, h);
            return new QuadSet(f, s, b, h);
        } catch (Exception ex) {
            throw new RuntimeException("EnemySpriteRenderer init failed for type '" + type + "': " + ex.getMessage(), ex);
        }
    }

    private static TexturedQuadRenderer selectRenderer(QuadSet qs, float deltaDeg) {
        float a = abs(deltaDeg);
        if (a <= 45f)  return qs.front;  // facing camera
        if (a >= 135f) return qs.back;   // facing away
        return qs.side;
    }

    private static float normalizeDeg(float a) {
        a %= 360f;
        if (a < -180f) a += 360f;
        if (a >  180f) a -= 360f;
        return a;
    }
}
