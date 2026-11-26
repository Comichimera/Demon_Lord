package game.ai;

import game.config.GameConfig;

import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Blackboard {
    public Optional<Vector2i> lastSeenPlayerCell = Optional.empty();
    public float timeSinceSeenPlayer = GameConfig.SOMETHING_HUGE;

    public Optional<Vector2i> lastHeardNoiseCell = Optional.empty();
    public float timeSinceHeardNoise = GameConfig.SOMETHING_HUGE;

    public float suspicion = 0f;

    public Optional<List<Vector2i>> currentPath = Optional.empty();

    public void clearPath() { currentPath = Optional.empty(); }
}