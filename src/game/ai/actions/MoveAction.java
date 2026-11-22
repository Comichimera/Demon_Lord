package game.ai.actions;

import game.ai.AIAgent;
import game.ai.Action;
import game.ai.Blackboard;
import game.ai.path.PathProvider;
import game.ai.path.PatrolProvider;
import game.ai.path.RandomAdjacentProvider;
import org.joml.Vector2i;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

public final class MoveAction implements Action {
    public enum Mode { PATH_PROVIDER, TOWARD_LAST_SEEN, TOWARD_LAST_HEARD }
    private final Mode mode;
    private final float speedMul;
    private final PathProvider provider;

    public static Action fromJson(JSONObject p) {
        float spd = (float)p.optDouble("speed", 1.0);
        if (p.has("toward")) {
            String t = p.getString("toward");
            switch (t) {
                case "lastSeenPlayer": return new MoveAction(Mode.TOWARD_LAST_SEEN, spd, null);
                case "lastHeardNoise": return new MoveAction(Mode.TOWARD_LAST_HEARD, spd, null);
                default: throw new IllegalArgumentException("Unknown 'toward': " + t);
            }
        }
        String mode = p.optString("mode", "randomAdjacent");
        PathProvider prov = "patrolOrRandom".equals(mode)
                ? new PatrolOrRandom()
                : new RandomAdjacentProvider();
        return new MoveAction(Mode.PATH_PROVIDER, spd, prov);
    }

    public MoveAction(Mode mode, float speedMul, PathProvider provider) {
        this.mode = mode; this.speedMul = speedMul; this.provider = provider;
    }

    @Override public Status tick(float dt, AIAgent agent, Blackboard bb) {
        switch (mode) {
            case TOWARD_LAST_SEEN:
                if (bb.lastSeenPlayerCell.isEmpty()) return Status.FAILURE;
                return stepToward(agent, bb, bb.lastSeenPlayerCell.get());
            case TOWARD_LAST_HEARD:
                if (bb.lastHeardNoiseCell.isEmpty()) return Status.FAILURE;
                return stepToward(agent, bb, bb.lastHeardNoiseCell.get());
            case PATH_PROVIDER:
            default:
                if (bb.currentPath.isEmpty()) {
                    Optional<List<Vector2i>> np = provider.nextPath(agent, bb);
                    if (np.isEmpty()) return Status.FAILURE;
                    bb.currentPath = np;
                }
                List<Vector2i> stored = bb.currentPath.get();
                Vector2i goal = stored.get(stored.size() - 1);
                if (agent.getCell().equals(goal)) {
                    bb.clearPath();
                    return Status.SUCCESS;
                }
                List<Vector2i> fresh = game.ai.path.AStar.findPath(agent.getCell(), goal, agent.world());
                if (fresh.isEmpty()) {
                    bb.clearPath();
                    return Status.FAILURE;
                }
                agent.requestMoveTo(fresh.get(0), speedMul);
                return Status.RUNNING;
        }
    }

    private Status stepToward(AIAgent agent, Blackboard bb, Vector2i target) {
        if (agent.getCell().equals(target)) return Status.SUCCESS;
        List<Vector2i> path = game.ai.path.AStar.findPath(agent.getCell(), target, agent.world());
        if (path.isEmpty()) return Status.FAILURE;
        agent.requestMoveTo(path.get(0), speedMul);
        return Status.RUNNING;
    }

    private static final class PatrolOrRandom implements PathProvider {
        private PatrolProvider patrol;
        private final RandomAdjacentProvider rand = new RandomAdjacentProvider();

        @Override public Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb) {
            if (patrol == null) {
                List<Vector2i> waypoints = agent.world().patrolWaypoints();
                if (!waypoints.isEmpty()) patrol = new PatrolProvider(waypoints);
            }
            if (patrol != null) {
                Optional<List<Vector2i>> p = patrol.nextPath(agent, bb);
                if (p.isPresent()) return p;
            }
            return rand.nextPath(agent, bb);
        }
    }
}
