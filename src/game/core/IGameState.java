package game.core;

public interface IGameState {
    void enter();

    void update(float dt);

    void render();

    void exit();
}
