package game.world.enemy;

import game.config.GameConfig;
import game.logic.EventBus;
import game.logic.GameEvent;
import game.logic.GameEventType;
import game.map.MapData;
import game.world.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnemyManager {
    private final List<Enemy> enemies = new ArrayList<>();
    private final EventBus events;
    private boolean playerDefeated = false;

    public EnemyManager(MapData map, EventBus events) {
        this.events = events;
        if (map.getEnemySpecs() != null) {
            for (JSONObject spec : map.getEnemySpecs()) {
                Enemy e = buildEnemyFromSpec(spec);
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

            if (e.canSeePlayer(map, player)) {
                final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                events.post(new GameEvent(GameEventType.ENEMY_SPOTTED_PLAYER, tx, ty));
            }

            final float dx = player.getX() - e.getX();
            final float dz = player.getZ() - e.getZ();
            final float dist2 = dx*dx + dz*dz;

            float r = (e instanceof BaseEnemy) ? ((BaseEnemy)e).getCaptureRadiusMeters() : (0.4f * GameConfig.TILE_SIZE);
            if (dist2 <= r * r) {
                playerDefeated = true;
                final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                events.post(new GameEvent(GameEventType.PLAYER_DEFEATED_BY_ENEMY, tx, ty));
                break;
            }

        }
    }

    private static Enemy buildEnemyFromSpec(JSONObject spec) {
        String type = spec.optString("type", "sentry").toLowerCase();

        switch (type) {
            case "patroller": {
                PatrollerEnemy p = new PatrollerEnemy();

                if (spec.has("speed")) p.speed = (float) spec.optDouble("speed", 1.5);
                // NEW: FOV & range (tiles)
                if (spec.has("fov"))          p.setFov((float) spec.optDouble("fov", 90.0));
                if (spec.has("viewDistance")) p.setViewDistTiles((float) spec.optDouble("viewDistance", 6.0));

                JSONObject spawn = spec.optJSONObject("spawn");
                if (spawn != null) {
                    int sx = spawn.optInt("x", 0);
                    int sy = spawn.optInt("y", 0);
                    p.setPosition(toWorld(sx), toWorld(sy));
                }

                JSONArray path = spec.optJSONArray("path");
                if (path != null) {
                    for (int i = 0; i < path.length(); i++) {
                        JSONObject node = path.getJSONObject(i);
                        p.addPathPointGrid(node.optInt("x", 0), node.optInt("y", 0));
                    }
                }

                if (spec.has("yaw")) p.setYaw((float) spec.optDouble("yaw", 0));
                return p;
            }
            case "sentry":
            default: {
                SentryEnemy s = new SentryEnemy();

                if (spec.has("speed")) s.speed = (float) spec.optDouble("speed", 0.0);
                if (spec.has("fov"))          s.setFov((float) spec.optDouble("fov", 90.0));       // NEW
                if (spec.has("viewDistance")) s.setViewDistTiles((float) spec.optDouble("viewDistance", 6.0)); // NEW

                JSONObject spawn = spec.optJSONObject("spawn");
                if (spawn != null) {
                    int sx = spawn.optInt("x", 0);
                    int sy = spawn.optInt("y", 0);
                    s.setPosition(toWorld(sx), toWorld(sy));
                }

                s.setYaw((float) spec.optDouble("yaw", 0.0));
                if (spec.has("turnRate")) s.setTurnRate((float) spec.optDouble("turnRate", 0.0));
                return s;
            }
        }
    }

    private static float toWorld(int grid) {
        return (grid + 0.5f) * GameConfig.TILE_SIZE;
    }
}
