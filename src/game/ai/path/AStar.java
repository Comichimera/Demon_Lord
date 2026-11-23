package game.ai.path;

import game.ai.IWorldAPI;
import org.joml.Vector2i;

import java.util.*;

public final class AStar {

    private AStar() {}

    public static List<Vector2i> findPath(Vector2i start, Vector2i goal, IWorldAPI world) {
        if (start.equals(goal)) return List.of();

        PriorityQueue<float[]> open = new PriorityQueue<>(Comparator.comparingDouble(e -> e[2]));
        Map<Vector2i, Float>   gScore   = new HashMap<>();
        Map<Vector2i, Vector2i> cameFrom = new HashMap<>();
        Set<Vector2i>           closed   = new HashSet<>();

        gScore.put(new Vector2i(start), 0f);
        open.add(new float[]{ start.x, start.y, world.heuristicCost(start, goal) });

        while (!open.isEmpty()) {
            float[] entry   = open.poll();
            Vector2i current = new Vector2i((int) entry[0], (int) entry[1]);

            if (closed.contains(current)) continue;
            closed.add(current);

            if (current.equals(goal)) return reconstruct(cameFrom, current);

            float g = gScore.getOrDefault(current, Float.MAX_VALUE);

            for (Vector2i nb : world.neighbors4(current)) {
                if (!world.isWalkable(nb) || closed.contains(nb)) continue;

                float ng = g + 1f;
                if (ng < gScore.getOrDefault(nb, Float.MAX_VALUE)) {
                    Vector2i nbKey = new Vector2i(nb);
                    gScore.put(nbKey, ng);
                    cameFrom.put(nbKey, new Vector2i(current));
                    open.add(new float[]{ nb.x, nb.y, ng + world.heuristicCost(nb, goal) });
                }
            }
        }

        return List.of();
    }

    private static List<Vector2i> reconstruct(Map<Vector2i, Vector2i> cameFrom, Vector2i end) {
        List<Vector2i> path = new ArrayList<>();
        Vector2i cur = end;
        while (cameFrom.containsKey(cur)) {
            path.add(new Vector2i(cur));
            cur = cameFrom.get(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
