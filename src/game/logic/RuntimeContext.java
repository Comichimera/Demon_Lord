package game.logic;
import game.map.Tile;
public interface RuntimeContext {
    Tile tileAt(int x, int y);               // returns current tile (null if OOB)
    float elapsedSeconds();                  // playing state timer
}