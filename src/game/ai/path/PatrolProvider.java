package game.ai.path;

import game.ai.AIAgent;
import game.ai.Blackboard;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

public final class PatrolProvider implements PathProvider {
    private final List<Vector2i> waypoints;
    private int idx = 0;
    public PatrolProvider(List<Vector2i> waypoints) { this.waypoints = waypoints; }

    @Override public Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb) {
        if (waypoints.isEmpty()) return Optional.empty();
        Vector2i target = waypoints.get(idx);
        idx = (idx + 1) % waypoints.size();
        if (agent.getCell().equals(target)) return Optional.empty();
        return Optional.of(List.of(new Vector2i(target)));
    }
}
