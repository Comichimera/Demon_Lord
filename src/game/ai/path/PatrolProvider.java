package game.ai.path;

import java.util.*;
import org.joml.Vector2i;
import game.ai.*;

public final class PatrolProvider implements PathProvider {
    private final List<Vector2i> waypoints;
    private int idx = 0;
    public PatrolProvider(List<Vector2i> waypoints) { this.waypoints = waypoints; }

    @Override public Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb) {
        if (waypoints.isEmpty()) return Optional.empty();
        Vector2i target = waypoints.get(idx);
        idx = (idx + 1) % waypoints.size();
        return Optional.of(java.util.List.of(new Vector2i(target)));
    }
}
