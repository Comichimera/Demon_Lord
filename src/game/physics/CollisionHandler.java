package game.physics;

import game.config.GameConfig;
import game.map.MapData;
import game.map.Tile;

public class CollisionHandler {
    public static final float TILE_SIZE = GameConfig.TILE_SIZE;
    public static final float MARGIN    = 0.25f;

    public static boolean isColliding(float x, float z, MapData mapData) {
        // Figure out which cell the player is in
        int col = (int)(x / TILE_SIZE);
        int row = (int)(z / TILE_SIZE);

        // Out of bounds = collision
        if (row < 0 || row >= mapData.getHeight() ||
                col < 0 || col >= mapData.getWidth()) {
            return true;
        }

        // If the tile itself is not walkable, that's a collision
        Tile current = mapData.getTile(row, col);
        if (!current.isWalkable()) {
            return true;
        }

        // Compute the edges of that cell
        float cellX0 = col * TILE_SIZE;
        float cellX1 = (col + 1) * TILE_SIZE;
        float cellZ0 = row * TILE_SIZE;
        float cellZ1 = (row + 1) * TILE_SIZE;

        // Check each boundary: if we're too close AND the adjacent tile is not walkable, collide.

        // Left edge
        if (x - cellX0 < MARGIN &&
                col - 1 >= 0 &&
                !mapData.getTile(row, col - 1).isWalkable()) {
            return true;
        }
        // Right edge
        if (cellX1 - x < MARGIN &&
                col + 1 < mapData.getWidth() &&
                !mapData.getTile(row, col + 1).isWalkable()) {
            return true;
        }
        // Top edge (smaller z)
        if (z - cellZ0 < MARGIN &&
                row - 1 >= 0 &&
                !mapData.getTile(row - 1, col).isWalkable()) {
            return true;
        }
        // Bottom edge (larger z)
        if (cellZ1 - z < MARGIN &&
                row + 1 < mapData.getHeight() &&
                !mapData.getTile(row + 1, col).isWalkable()) {
            return true;
        }

        // No collision
        return false;
    }
}
