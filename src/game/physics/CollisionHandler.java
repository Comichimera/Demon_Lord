package game.physics;

import game.config.GameConfig;
import game.map.MapData;
import game.map.Tile;

public class CollisionHandler {
    public static final float TILE_SIZE = GameConfig.TILE_SIZE;
    public static final float MARGIN    = 0.25f;

    public static boolean isColliding(float x, float z, MapData mapData) {
        int col = (int) Math.floor(x / TILE_SIZE);
        int row = (int) Math.floor(z / TILE_SIZE);

        if (row < 0 || row >= mapData.getHeight() ||
                col < 0 || col >= mapData.getWidth()) {
            return true;
        }

        Tile current = mapData.getTile(row, col);
        if (!current.isWalkable()) {
            return true;
        }

        float cellX0 = col * TILE_SIZE;
        float cellX1 = (col + 1) * TILE_SIZE;
        float cellZ0 = row * TILE_SIZE;
        float cellZ1 = (row + 1) * TILE_SIZE;

        if (x - cellX0 < MARGIN &&
                col - 1 >= 0 &&
                !mapData.getTile(row, col - 1).isWalkable()) {
            return true;
        }
        if (cellX1 - x < MARGIN &&
                col + 1 < mapData.getWidth() &&
                !mapData.getTile(row, col + 1).isWalkable()) {
            return true;
        }
        if (z - cellZ0 < MARGIN &&
                row - 1 >= 0 &&
                !mapData.getTile(row - 1, col).isWalkable()) {
            return true;
        }
        if (cellZ1 - z < MARGIN &&
                row + 1 < mapData.getHeight() &&
                !mapData.getTile(row + 1, col).isWalkable()) {
            return true;
        }

        return false;
    }
}
