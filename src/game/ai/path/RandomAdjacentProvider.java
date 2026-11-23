package game.ai.path;

import game.ai.IAIAgent;
import game.ai.Blackboard;
import org.joml.Vector2i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class RandomAdjacentProvider implements IPathProvider {
    @Override public Optional<List<Vector2i>> nextPath(IAIAgent agent, Blackboard bb) {
        List<Vector2i> ns = agent.world().neighbors4(agent.getCell());
        Collections.shuffle(ns, agent.rng());
        for (Vector2i n : ns) if (agent.world().isWalkable(n)) {
            return Optional.of(java.util.List.of(new Vector2i(n)));
        }
        return Optional.empty();
    }
}
