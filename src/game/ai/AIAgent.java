package game.ai;

import java.util.Random;
import org.joml.Vector2i;

public interface AIAgent {
    Vector2i getCell();

    void requestMoveTo(Vector2i nextCell, float speedMul);

    void aimAt(Vector2i cell);

    Random rng();

    WorldAPI world();
}