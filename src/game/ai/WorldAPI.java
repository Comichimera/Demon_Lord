package game.ai;

import java.util.List;
import org.joml.Vector2i;

public interface WorldAPI {
    boolean isWalkable(Vector2i cell);
    boolean hasLineOfSight(Vector2i from, Vector2i to);
    List<Vector2i> neighbors4(Vector2i cell); // N/E/S/W
    float heuristicCost(Vector2i a, Vector2i b); // e.g., manhattan
    float seconds(); // game time for timestamps
}
