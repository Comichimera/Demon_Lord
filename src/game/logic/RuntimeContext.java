package game.logic;
import game.map.Tile;
public interface RuntimeContext {
    Tile tileAt(int x, int y);
    float elapsedSeconds();
}