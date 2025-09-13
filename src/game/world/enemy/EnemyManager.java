package game.world.enemy;

import game.config.GameConfig;
import game.logic.EventBus;
import game.logic.GameEvent;
import game.logic.GameEventType;
import game.map.MapData;
import game.world.Player;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnemyManager {

    private final List<Enemy> enemies = new ArrayList<>();
    private boolean playerDefeated = false;
    private final EventBus events;
    private final Player player;

    public EnemyManager(MapData map, Player player, EventBus events) {
        this.events = events;
        this.player = player;
        if (map.getEnemySpecs() != null) {
            for (JSONObject spec : map.getEnemySpecs()) {
                Enemy e = buildEnemyFromSpec(spec, map, events);
                if (e != null) enemies.add(e);
            }
        }
    }

    public List<Enemy> getEnemies() { return Collections.unmodifiableList(enemies); }

    public boolean isPlayerDefeated() { return playerDefeated; }

    public void update(float dt, MapData map, Player player) {
        if (playerDefeated) return;

        for (Enemy e : enemies) {
            e.update(dt, map, player, events);

            // Vision events: now AI-backed via EnemyEntity.canSeePlayer(...)
            if (e.canSeePlayer(map, player)) {
                final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                events.post(new GameEvent(GameEventType.ENEMY_SPOTTED_PLAYER, tx, ty));
            }

            // Capture / defeat check (unchanged)
            final float dx = player.getX() - e.getX();
            final float dz = player.getZ() - e.getZ();
            final float dist2 = dx*dx + dz*dz;

            float r = (e instanceof BaseEnemy)
                    ? ((BaseEnemy)e).getCaptureRadiusMeters()
                    : (0.4f * GameConfig.TILE_SIZE);

            if (dist2 <= r * r) {
                playerDefeated = true;
                final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                events.post(new GameEvent(GameEventType.PLAYER_DEFEATED_BY_ENEMY, tx, ty));
                break;
            }
        }
    }
    private Enemy buildEnemyFromSpec(JSONObject spec, MapData map, EventBus events) {
        final String legacyType = spec.optString("type", "sentry").toLowerCase();

        final String spriteKey = spec.optString("sprite", legacyType);

        String behavior = spec.optString("behavior", null);
        if (behavior == null) {
            switch (legacyType) {
                case "patroller":
                    behavior = "/data/ai/patroller.behavior.json";
                    break;
                case "sentry":
                default:
                    behavior = "/data/ai/sentry.behavior.json";
                    break;
            }
        }

        // Movement/vision parameters
        final float speed = (float) spec.optDouble("speed", 1.5);
        final float fov   = (float) spec.optDouble("fov", 90.0);
        final float view  = (float) spec.optDouble("viewDistance", 6.0);
        final float yaw   = (float) spec.optDouble("yaw", 0.0);

        // Spawn (grid coords)
        final JSONObject spawn = spec.optJSONObject("spawn");
        final int sx = spawn != null ? spawn.optInt("x", 0) : 0;
        final int sy = spawn != null ? spawn.optInt("y", 0) : 0;

        final List<Vector2i> patrol = new ArrayList<>();
        if (spec.has("patrol")) {
            JSONArray arr = spec.getJSONArray("patrol");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject node = arr.getJSONObject(i);
                patrol.add(new Vector2i(node.optInt("x", 0), node.optInt("y", 0)));
            }
        } else if (spec.has("path")) {
            JSONArray arr = spec.getJSONArray("path");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject node = arr.getJSONObject(i);
                patrol.add(new Vector2i(node.optInt("x", 0), node.optInt("y", 0)));
            }
        }

        final InputStream behaviorJson = EnemyManager.class.getResourceAsStream(behavior);
        if (behaviorJson == null) {
            throw new IllegalArgumentException("Behavior file not found on classpath: " + behavior);
        }

        // Deterministic-ish seed so wandering is stable between runs
        final long seed = ((long)sx * 73856093L) ^ ((long)sy * 19349663L) ^ behavior.hashCode();

        EnemyEntity e = new EnemyEntity(
                spriteKey,
                behaviorJson,
                patrol,
                map,
                player,
                events,
                speed,
                fov,
                view,
                yaw,
                seed
        );

        // Position in world units (center of tile)
        e.setPosition(toWorld(sx), toWorld(sy));
        e.setYaw(yaw);
        return e;
    }

    private static float toWorld(int grid) {
        return (grid + 0.5f) * GameConfig.TILE_SIZE;
    }
}
