package game.world.enemy;

import game.ai.*;
import game.ai.bt.Node;
import game.ai.io.BehaviorTreeLoader;
import game.ai.sensors.*;
import game.ai.path.*;
import game.logic.EventBus;
import game.map.MapData;
import game.world.Player;
import org.json.JSONObject;
import org.joml.Vector2i;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class EnemyEntity extends BaseEnemy implements Enemy, AIAgent {
    private final BehaviorController ai;
    private final Random rng;
    private final WorldAdapter world;     // wraps MapData+Player+EventBus
    private final String spriteKey;       // used by EnemySpriteRenderer cache

    private Vector2i moveIntentCell = null;
    private float moveSpeedMul = 1f;
    private Vector2i aimTarget = null;

    public EnemyEntity(String spriteKey,
                       InputStream behaviorJson,
                       List<Vector2i> patrol,
                       MapData map, Player player, EventBus events,
                       float speed, float fovDeg, float viewTiles,
                       float yawDegrees,
                       long seed) {
        super(spriteKey);
        this.spriteKey = spriteKey;
        this.speed = speed;
        this.fovDeg = fovDeg;
        this.viewDistTiles = viewTiles;
        this.yaw = yawDegrees;

        this.rng = new Random(seed);
        this.world = new WorldAdapter(map, player);

        List<Sensor> sensors = List.of(
                new VisionSensor(fovDeg, viewTiles),
                new HearingSensor(3.0f)
        );

        this.ai = BehaviorController.fromJsonResource(behaviorJson, sensors, 10f);

        world.setPatrol(patrol); // used by PathProvider if needed
    }

    @Override public void update(float dt, MapData map, Player player, EventBus events) {
        world.accumulateTime(dt);
        ai.tick(dt, this);
        // Apply movement intent
        if (moveIntentCell != null) {
            float cx = (moveIntentCell.x + 0.5f) * game.config.GameConfig.TILE_SIZE;
            float cz = (moveIntentCell.y + 0.5f) * game.config.GameConfig.TILE_SIZE;
            float dx = cx - x, dz = cz - z;
            float len = (float)Math.sqrt(dx*dx + dz*dz);
            if (len > 1e-4f) {
                float nx = dx/len, nz = dz/len;
                move(nx * speed * moveSpeedMul * dt, nz * speed * moveSpeedMul * dt, map);
                faceToward(cx, cz);
            } else {
                moveIntentCell = null; // reached
            }
        }
    }

    @Override public boolean canSeePlayer(MapData map, Player player) {
        float recent = 0.2f; // seconds
        return ai.bb().lastSeenPlayerCell.isPresent() && ai.bb().timeSinceSeenPlayer <= recent;
    }
    @Override public org.joml.Vector2i getCell() {
        int tx = (int)(x / game.config.GameConfig.TILE_SIZE);
        int ty = (int)(z / game.config.GameConfig.TILE_SIZE);
        return new Vector2i(tx, ty);
    }
    @Override public void requestMoveTo(Vector2i nextCell, float speedMul) {
        this.moveIntentCell = new Vector2i(nextCell);
        this.moveSpeedMul = speedMul;
    }
    @Override public void aimAt(Vector2i cell) { this.aimTarget = new Vector2i(cell); }
    @Override public Random rng() { return rng; }
    @Override public WorldAPI world() { return world; }

    @Override public String getType() { return spriteKey; } // drives sprite set

    @Override
    public float getY() { return 0f; }
}