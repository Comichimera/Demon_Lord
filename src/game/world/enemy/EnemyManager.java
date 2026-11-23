package game.world.enemy;

import game.config.GameConfig;
import game.logic.event.EventBus;
import game.logic.event.GameEvent;
import game.logic.event.EGameEventType;
import game.map.MapData;
import game.world.Player;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

public class EnemyManager {

    private final List<IEnemy> enemies = new ArrayList<>();
    private boolean playerDefeated = false;
    private final Set<IEnemy> spottingLastFrame = new HashSet<>();
    private final EventBus events;
    private final Player player;

    public EnemyManager(MapData map, Player player, EventBus events) {
        this.events = events;
        this.player = player;
        if (map.getEnemySpecs() != null) {
            for (JSONObject spec : map.getEnemySpecs()) {
                IEnemy e = buildEnemyFromSpec(spec, map, events);
                if (e != null) enemies.add(e);
            }
        }
    }

    public List<IEnemy> getEnemies() { return Collections.unmodifiableList(enemies); }

    public boolean isPlayerDefeated() { return playerDefeated; }

    public int getAliveCount() { return enemies.size(); }

    public boolean isPlayerSpotted() { return !spottingLastFrame.isEmpty(); }

    public float nearestEnemyDistanceTiles(Player player) {
        if (enemies.isEmpty()) return -1f;
        float min2 = Float.MAX_VALUE;
        for (IEnemy e : enemies) {
            float dx = e.getX() - player.getX(), dz = e.getZ() - player.getZ();
            float d2 = dx * dx + dz * dz;
            if (d2 < min2) min2 = d2;
        }
        return (float) Math.sqrt(min2) / GameConfig.TILE_SIZE;
    }

    public void update(float dt, MapData map, Player player) {
        if (playerDefeated) return;

        Set<IEnemy> spottingNow = new HashSet<>();
        for (IEnemy e : enemies) {
            e.update(dt, map, player, events);

            if (e.canSeePlayer(map, player)) {
                spottingNow.add(e);
                if (!spottingLastFrame.contains(e)) {
                    final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                    final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                    events.post(new GameEvent(EGameEventType.ENEMY_SPOTTED_PLAYER, tx, ty));
                }
            }

            final float dx = player.getX() - e.getX();
            final float dz = player.getZ() - e.getZ();
            final float dist2 = dx*dx + dz*dz;

            float r = (e instanceof BaseEnemy)
                    ? ((BaseEnemy)e).getCaptureRadiusMeters()
                    : (0.4f * GameConfig.TILE_SIZE);

            if (dist2 <= r * r && e instanceof BaseEnemy) {
                BaseEnemy be = (BaseEnemy) e;
                if (be.tryAttack(dt)) {
                    final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
                    final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
                    float severity = Math.min(1.0f, be.getAttackDamage() / 30f);
                    player.applyHit(be.getAttackDamage(), severity);
                    events.post(new GameEvent(EGameEventType.PLAYER_TOOK_DAMAGE, tx, ty));
                }
            }
        }

        enemies.removeIf(e -> (e instanceof BaseEnemy) && !((BaseEnemy) e).isAlive());

        spottingLastFrame.clear();
        spottingLastFrame.addAll(spottingNow);

        if (!player.isAlive() && !playerDefeated) {
            playerDefeated = true;
            final int tx = (int)(player.getX() / GameConfig.TILE_SIZE);
            final int ty = (int)(player.getZ() / GameConfig.TILE_SIZE);
            events.post(new GameEvent(EGameEventType.PLAYER_DEFEATED_BY_ENEMY, tx, ty));
        }
    }
    private IEnemy buildEnemyFromSpec(JSONObject spec, MapData map, EventBus events) {
        final String legacyType = spec.optString("type", "sentry").toLowerCase();

        final String spriteKey = spec.optString("sprite", legacyType);

        String behavior = spec.optString("behavior", null);
        if (behavior == null) {
            behavior = "/data/ai/" + legacyType + ".behavior.json";
        }

        final float speed = (float) spec.optDouble("speed", 1.5);
        final float fov   = (float) spec.optDouble("fov", 90.0);
        final float view  = (float) spec.optDouble("viewDistance", 6.0);
        final float yaw   = (float) spec.optDouble("yaw", 0.0);

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

        int hp     = spec.optInt("hp", 30);
        int atkDmg = spec.optInt("attackDamage", 15);
        e.initHp(hp);
        e.setAttackDamage(atkDmg);

        e.setPosition(toWorld(sx), toWorld(sy));
        e.setYaw(yaw);
        return e;
    }

    private static float toWorld(int grid) {
        return (grid + 0.5f) * GameConfig.TILE_SIZE;
    }
}
