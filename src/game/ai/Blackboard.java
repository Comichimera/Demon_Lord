package game.ai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.joml.Vector2i;

public final class Blackboard {
    public Optional<Vector2i> lastSeenPlayerCell = Optional.empty();
    public float timeSinceSeenPlayer = Float.POSITIVE_INFINITY;

    public Optional<Vector2i> lastHeardNoiseCell = Optional.empty();
    public float timeSinceHeardNoise = Float.POSITIVE_INFINITY;

    public float suspicion = 0f;

    public Optional<List<Vector2i>> currentPath = Optional.empty();
    public int currentPathIndex = 0;

    public final Deque<String> debugNotes = new ArrayDeque<>();

    public void clearPath() { currentPath = Optional.empty(); currentPathIndex = 0; }
}