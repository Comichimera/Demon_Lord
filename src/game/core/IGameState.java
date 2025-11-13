package game.core;

public interface IGameState {
    // Called once when this state becomes active.
    void enter();

    // Called every frame; dt is seconds since last frame.
    void update(float dt);

    // Called every frame to draw this state’s UI or scene.
    void render();

    // Called once when this state is about to be replaced.
    void exit();
}
