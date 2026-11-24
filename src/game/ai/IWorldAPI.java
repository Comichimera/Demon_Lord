package game.ai;

import org.joml.Vector2i;

import java.util.List;

public interface IWorldAPI {
    boolean isWalkable(Vector2i cell);
    boolean hasLineOfSight(Vector2i from, Vector2i to);
    List<Vector2i> neighbors4(Vector2i cell);
    float heuristicCost(Vector2i a, Vector2i b);
    Vector2i playerCell();
    Vector2i lastNoiseCell();
    List<Vector2i> patrolWaypoints();
}
