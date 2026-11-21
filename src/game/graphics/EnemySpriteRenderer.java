package game.graphics;

import game.config.GameConfig;
import game.world.Player;
import game.world.enemy.Enemy;
import game.world.enemy.EnemyManager;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

public final class EnemySpriteRenderer {

    private static final class QuadSet {
        final TexturedQuadRenderer front, sideLeft, sideRight, back;
        final SpriteDefsLoader.SpriteDef def;
        final float heightMeters;

        QuadSet(TexturedQuadRenderer f, TexturedQuadRenderer sl, TexturedQuadRenderer sr, TexturedQuadRenderer b,
                SpriteDefsLoader.SpriteDef def, float hMeters) {
            front = f; sideLeft = sl; sideRight = sr; back = b;
            this.def = def;
            this.heightMeters = hMeters;
        }

        void cleanup() { front.cleanup(); sideLeft.cleanup(); sideRight.cleanup(); back.cleanup(); }
    }

    private final SpriteDefsLoader defs;
    private final Map<String, QuadSet> cache = new HashMap<>();

    public EnemySpriteRenderer(SpriteDefsLoader defs) {
        this.defs = defs;
    }

    public void render(EnemyManager manager, Player cameraPlayer, Matrix4f projection, Matrix4f view, float timeSeconds) {
        if (manager == null) return;

        boolean wasBlend = glIsEnabled(GL_BLEND);
        boolean wasCull  = glIsEnabled(GL_CULL_FACE);

        if (!wasBlend) glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (wasCull) glDisable(GL_CULL_FACE);

        final float camYaw = cameraPlayer.getYaw();

        for (Enemy e : manager.getEnemies()) {
            QuadSet qs = cache.computeIfAbsent(e.getType().toLowerCase(), this::buildQuadSetForType);

            float delta = normalizeDeg(e.getYaw() - camYaw);
            float a = abs(delta);

            TexturedQuadRenderer chosenRend;
            SpriteDefsLoader.AnimDef anim;

            if (a <= 45f) {
                chosenRend = qs.front;
                anim = qs.def.front;
            } else if (a >= 135f) {
                chosenRend = qs.back;
                anim = qs.def.back;
            } else if (delta > 0f) {
                chosenRend = qs.sideLeft;
                anim = qs.def.sideLeft;
            } else {
                chosenRend = qs.sideRight;
                anim = qs.def.sideRight;
            }

            float phase = (System.identityHashCode(e) & 0xFFFF) / 65535f * 10f;
            float t = timeSeconds + phase;

            int frameIndex = frameAt(anim, t);

            float uvScaleX = 1f / anim.cols;
            float uvScaleY = 1f / anim.rows;

            int col = frameIndex % anim.cols;
            int row = frameIndex / anim.cols;

            float uvOffX = col * uvScaleX;
            float uvOffY = row * uvScaleY;

            float hoverMeters = qs.def.hoverTiles * GameConfig.TILE_SIZE;
            float bobMeters = 0f;
            if (qs.def.bobAmpTiles != 0f && qs.def.bobHz != 0f) {
                bobMeters = (float)(qs.def.bobAmpTiles * GameConfig.TILE_SIZE *
                        sin(2.0 * Math.PI * qs.def.bobHz * t));
            }

            Matrix4f model = new Matrix4f()
                    .translation(e.getX(), qs.heightMeters * 0.5f + hoverMeters + bobMeters, e.getZ())
                    .rotateY((float) toRadians(-camYaw))
                    .rotateX((float) toRadians(90.0));

            chosenRend.render(projection, view, model, uvOffX, uvOffY, uvScaleX, uvScaleY);
        }

        if (wasCull) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE);
        if (!wasBlend) glDisable(GL_BLEND);
    }

    public void cleanup() {
        for (QuadSet qs : cache.values()) qs.cleanup();
        cache.clear();
    }

    private QuadSet buildQuadSetForType(String type) {
        SpriteDefsLoader.SpriteDef d = defs.get(type);
        float w = d.widthTiles  * GameConfig.TILE_SIZE;
        float h = d.heightTiles * GameConfig.TILE_SIZE;

        try {
            TexturedQuadRenderer f  = new TexturedQuadRenderer(d.front.sheetPath,     w, h);
            TexturedQuadRenderer sl = new TexturedQuadRenderer(d.sideLeft.sheetPath,  w, h);
            TexturedQuadRenderer sr = new TexturedQuadRenderer(d.sideRight.sheetPath, w, h);
            TexturedQuadRenderer b  = new TexturedQuadRenderer(d.back.sheetPath,      w, h);
            return new QuadSet(f, sl, sr, b, d, h);
        } catch (Exception ex) {
            throw new RuntimeException("EnemySpriteRenderer init failed for '" + type + "': " + ex.getMessage(), ex);
        }
    }

    private static TexturedQuadRenderer selectRenderer(QuadSet qs, float deltaDeg) {
        float a = abs(deltaDeg);
        if (a <= 45f)  return qs.front;
        if (a >= 135f) return qs.back;
        return (deltaDeg > 0f) ? qs.sideLeft : qs.sideRight;
    }

    private static int frameAt(SpriteDefsLoader.AnimDef a, float tSeconds) {
        if (a.count <= 1) return a.start;

        if (a.frameMs == null) {
            int local = (int) Math.floor(tSeconds * a.fps) % a.count;
            if (local < 0) local += a.count;
            return a.start + local;
        }

        int totalMs = 0;
        for (int ms : a.frameMs) totalMs += ms;
        if (totalMs <= 0) return a.start;

        int tMs = (int) (tSeconds * 1000f);
        int m = tMs % totalMs;
        if (m < 0) m += totalMs;

        int acc = 0;
        for (int i = 0; i < a.frameMs.length; i++) {
            acc += a.frameMs[i];
            if (m < acc) {
                int local = i % a.count;
                return a.start + local;
            }
        }

        return a.start;
    }

    private static float normalizeDeg(float a) {
        a %= 360f;
        if (a < -180f) a += 360f;
        if (a >  180f) a -= 360f;
        return a;
    }
}
