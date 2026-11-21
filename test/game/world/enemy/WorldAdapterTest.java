package game.world.enemy;

import game.map.MapData;
import game.map.Tile;
import game.map.TileDefinition;
import game.world.Player;
import org.joml.Vector2i;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class WorldAdapterTest {

    private static TileDefinition floorDef() {
        return new TileDefinition('F', "Floor", true, false, false, "floor", "dummy.png", "floor");
    }

    private static TileDefinition wallDef() {
        return new TileDefinition('W', "Wall", false, false, false, "cube", "dummy.png", "wall");
    }

    private static MapData oneRowWithWallInMiddle() {
        Tile[][] tiles = new Tile[1][5];
        tiles[0][0] = new Tile(floorDef());
        tiles[0][1] = new Tile(floorDef());
        tiles[0][2] = new Tile(wallDef());
        tiles[0][3] = new Tile(floorDef());
        tiles[0][4] = new Tile(floorDef());

        return new MapData(
                tiles,
                0, 0, 0f,
                Collections.<JSONObject>emptyList(),
                Collections.<JSONObject>emptyList(),
                "test"
        );
    }

    @Test
    void isWalkableMatchesTileWalkable() {
        MapData map = oneRowWithWallInMiddle();
        Player p = new Player();
        WorldAdapter w = new WorldAdapter(map, p);

        assertTrue(w.isWalkable(new Vector2i(0, 0)));
        assertFalse(w.isWalkable(new Vector2i(2, 0)));
        assertTrue(w.isWalkable(new Vector2i(4, 0)));
    }

    @Disabled("Enable after implementing LOS occlusion against non-walkable tiles")
    @Test
    void lineOfSightBlockedByWall() {
        MapData map = oneRowWithWallInMiddle();
        Player p = new Player();
        WorldAdapter w = new WorldAdapter(map, p);

        assertFalse(w.hasLineOfSight(new Vector2i(0, 0), new Vector2i(4, 0)));
    }
}
