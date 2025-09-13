package game.ai.actions;

import java.util.*;
import org.json.JSONObject;
import org.joml.Vector2i;
import game.ai.*;
import game.ai.path.*;

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
                ? new CompositeProvider(new PatrolOrRandom())
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
                if (bb.currentPath.isEmpty() || bb.currentPathIndex >= bb.currentPath.get().size()) {
                    Optional<List<Vector2i>> np = provider.nextPath(agent, bb);
                    if (np.isEmpty()) return Status.FAILURE;
                    bb.currentPath = np; bb.currentPathIndex = 0;
                }
                List<Vector2i> path = bb.currentPath.get();
                Vector2i next = path.get(bb.currentPathIndex);
                if (agent.getCell().equals(next)) {
                    bb.currentPathIndex++;
                    if (bb.currentPathIndex >= path.size()) { bb.clearPath(); return Status.SUCCESS; }
                    next = path.get(bb.currentPathIndex);
                }
                agent.requestMoveTo(next, speedMul);
                return Status.RUNNING;
        }
    }

    private Status stepToward(AIAgent agent, Blackboard bb, Vector2i target) {
        java.util.List<Vector2i> ns = agent.world().neighbors4(agent.getCell());
        ns.sort(java.util.Comparator.comparingDouble(n -> agent.world().heuristicCost(n, target)));
        for (Vector2i n : ns) if (agent.world().isWalkable(n)) {
            agent.requestMoveTo(n, speedMul);
            return Status.RUNNING;
        }
        return Status.FAILURE;
    }

    private static final class PatrolOrRandom implements PathProvider {
        private final PatrolProvider patrol;
        private final RandomAdjacentProvider rand = new RandomAdjacentProvider();
        PatrolOrRandom() { this.patrol = null; }
        @Override public Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb) {
            if (patrol != null) {
                Optional<List<Vector2i>> p = patrol.nextPath(agent, bb);
                if (p.isPresent()) return p;
            }
            return rand.nextPath(agent, bb);
        }
    }
    private static final class CompositeProvider implements PathProvider {
        private final PathProvider p;
        CompositeProvider(PathProvider p) { this.p = p; }
        @Override public Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb) { return p.nextPath(agent, bb); }
    }
}
