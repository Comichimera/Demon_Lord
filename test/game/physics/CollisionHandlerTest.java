package game.physics;

import game.config.GameConfig;
import game.map.MapData;
import game.map.Tile;
import game.map.TileDefinition;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CollisionHandlerTest {

    private static TileDefinition floorDef() {
        return new TileDefinition('F', "Floor", true, false, false, "floor", "dummy.png", "floor");
    }

    private static TileDefinition wallDef() {
        return new TileDefinition('W', "Wall", false, false, false, "cube", "dummy.png", "wall");
    }

    private static MapData mapFromWalkable(boolean[][] walkable) {
        int h = walkable.length;
        int w = walkable[0].length;

        Tile[][] tiles = new Tile[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                tiles[y][x] = new Tile(walkable[y][x] ? floorDef() : wallDef());
            }
        }

        return new MapData(
                tiles,
                0, 0, 0f,
                Collections.<JSONObject>emptyList(),
                Collections.<JSONObject>emptyList(),
                "test"
        );
    }

    @Test
    void outOfBoundsIsCollision() {
        MapData map = mapFromWalkable(new boolean[][]{
                { true, true },
                { true, true }
        });

        float ts = GameConfig.TILE_SIZE;

        assertTrue(CollisionHandler.isColliding(-0.01f, ts * 0.5f, map));
        assertTrue(CollisionHandler.isColliding(ts * 0.5f, -0.01f, map));
        assertTrue(CollisionHandler.isColliding(ts * 2 + 0.01f, ts * 0.5f, map));
        assertTrue(CollisionHandler.isColliding(ts * 0.5f, ts * 2 + 0.01f, map));
    }

    @Test
    void standingInNonWalkableTileIsCollision() {
        MapData map = mapFromWalkable(new boolean[][]{
                { false, true },
                { true,  true }
        });

        float ts = GameConfig.TILE_SIZE;

        assertTrue(CollisionHandler.isColliding(ts * 0.5f, ts * 0.5f, map));
    }

    @Test
    void marginNearWallCollidesButCenterDoesNot() {
        MapData map = mapFromWalkable(new boolean[][]{
                { false, true }
        });

        float ts = GameConfig.TILE_SIZE;

        float nearLeftEdgeX = ts + (CollisionHandler.MARGIN / 2f);
        float z = ts * 0.5f;

        assertTrue(CollisionHandler.isColliding(nearLeftEdgeX, z, map));

        float centerX = ts + (ts / 2f);
        assertFalse(CollisionHandler.isColliding(centerX, z, map));
    }
}
