package game.ai;

import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Blackboard {
    public Optional<Vector2i> lastSeenPlayerCell = Optional.empty();
    public float timeSinceSeenPlayer = Float.POSITIVE_INFINITY;

    public Optional<Vector2i> lastHeardNoiseCell = Optional.empty();
    public float timeSinceHeardNoise = Float.POSITIVE_INFINITY;

    public float suspicion = 0f;

    public Optional<List<Vector2i>> currentPath = Optional.empty();

    public void clearPath() { currentPath = Optional.empty(); }
}