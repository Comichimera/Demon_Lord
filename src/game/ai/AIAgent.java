package game.ai;

import java.util.Random;
import org.joml.Vector2i;

public interface AIAgent {
    // Position on your grid
    Vector2i getCell();

    // Request a move to a neighbor (or next path cell) at speed multiplier
    void requestMoveTo(Vector2i nextCell, float speedMul);

    // Aim head/body toward a cell (visual only; you can implement as needed)
    void aimAt(Vector2i cell);

    // Access to shared RNG for determinism
    Random rng();

    // Bridge to world queries
    WorldAPI world();
}