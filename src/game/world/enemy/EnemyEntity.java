package game.world.enemy;

import game.ai.IAIAgent;
import game.ai.BehaviorController;
import game.ai.ISensor;
import game.ai.IWorldAPI;
import game.ai.sensors.HearingSensor;
import game.ai.sensors.VisionSensor;
import game.logic.event.EventBus;
import game.map.MapData;
import game.world.Player;
import org.joml.Vector2i;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

public final class EnemyEntity extends BaseEnemy implements IEnemy, IAIAgent {
    private final BehaviorController ai;
    private final Random rng;
    private final WorldAdapter world;
    private final String spriteKey;

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

        List<ISensor> sensors = List.of(
                new VisionSensor(fovDeg, viewTiles),
                new HearingSensor(3.0f),
                new game.ai.sensors.SuspicionSensor()
        );

        this.ai = BehaviorController.fromJsonResource(behaviorJson, sensors, 10f);

        world.setPatrol(patrol);
    }

    @Override public void update(float dt, MapData map, Player player, EventBus events) {
        world.accumulateTime(dt);
        ai.tick(dt, this);

        if (moveIntentCell == null && aimTarget != null) {
            float cx = (aimTarget.x + 0.5f) * game.config.GameConfig.TILE_SIZE;
            float cz = (aimTarget.y + 0.5f) * game.config.GameConfig.TILE_SIZE;
            faceToward(cx, cz);
        }
        aimTarget = null;

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
                moveIntentCell = null;
            }
        }
    }

    @Override public boolean canSeePlayer(MapData map, Player player) {
        float recent = 0.2f;
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
    @Override public IWorldAPI world() { return world; }

    @Override public String getType() { return spriteKey; }

    @Override public float getYawDeg() { return getYaw(); }

    @Override
    public float getY() { return 0f; }
}