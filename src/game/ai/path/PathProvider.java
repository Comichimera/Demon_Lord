package game.ai.path;

import java.util.List;
import java.util.Optional;
import org.joml.Vector2i;
import game.ai.*;

public interface PathProvider {
    Optional<List<Vector2i>> nextPath(AIAgent agent, Blackboard bb);
}
