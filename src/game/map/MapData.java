package game.map;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private final Tile[][] tiles;
    private final int playerSpawnX;
    private final int playerSpawnY;
    private final float  cameraYaw;

    public MapData(Tile[][] tiles, int playerSpawnX, int playerSpawnY, float cameraYaw) {
        this.tiles = tiles;
        this.playerSpawnX = playerSpawnX;
        this.playerSpawnY = playerSpawnY;
        this.cameraYaw = cameraYaw;
    }

    // Returns the entire tile grid.
    public Tile[][] getTiles() {
        return tiles;
    }

    // Returns the Tile at (row,col).
    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public int getWidth() {
        return tiles[0].length;
    }
    public int getHeight() {
        return tiles.length;
    }

    public int getPlayerSpawnX() { return playerSpawnX; }
    public int getPlayerSpawnY() { return playerSpawnY; }
    public float  getCameraYaw()  { return cameraYaw; }
}
