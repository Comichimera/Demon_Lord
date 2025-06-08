package game.map;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private final Tile[][] tiles;
    private final int playerSpawnX;
    private final int playerSpawnY;
    private final String wallTexturePath;
    private final String exitTexturePath;
    private final float  cameraYaw;
    private final String doorTexturePath;

    public MapData(Tile[][] tiles,
                   int playerSpawnX, int playerSpawnY,
                   String wallTexturePath,
                   String exitTexturePath,
                   float cameraYaw,
                   String doorTexturePath) {
        this.tiles            = tiles;
        this.playerSpawnX     = playerSpawnX;
        this.playerSpawnY     = playerSpawnY;
        this.wallTexturePath  = wallTexturePath;
        this.exitTexturePath  = exitTexturePath;
        this.cameraYaw        = cameraYaw;
        this.doorTexturePath  = doorTexturePath;
    }

    /** Returns the entire tile grid. */
    public Tile[][] getTiles() {
        return tiles;
    }

    /** Returns the Tile at (row,col). */
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
    public String getWallTexturePath() { return wallTexturePath; }
    public String getExitTexturePath() { return exitTexturePath; }
    public String getDoorTexturePath() { return doorTexturePath; }
}
