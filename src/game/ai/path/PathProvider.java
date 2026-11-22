package game.ai.path;

import game.ai.AIAgent;
import game.ai.Blackboard;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

public interface PathProvider {
    Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb);
}
