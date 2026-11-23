package game.logic.objective;
import game.map.Tile;
public interface IRuntimeContext {
    Tile tileAt(int x, int y);
    float elapsedSeconds();
}