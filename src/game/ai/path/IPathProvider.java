package game.ai.path;

import game.ai.IAIAgent;
import game.ai.Blackboard;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

public interface IPathProvider {
    Optional<List<Vector2i>> nextPath(IAIAgent agent, Blackboard bb);
}
