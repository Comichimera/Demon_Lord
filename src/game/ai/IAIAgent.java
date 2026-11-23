package game.ai;

import org.joml.Vector2i;

import java.util.Random;

public interface IAIAgent {
    Vector2i getCell();

    void requestMoveTo(Vector2i nextCell, float speedMul);

    void aimAt(Vector2i cell);

    float getYawDeg();

    void faceToward(float worldX, float worldZ);

    Random rng();

    IWorldAPI world();
}