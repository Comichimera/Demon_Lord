package game.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TileTest {

    private static TileDefinition floorDef() {
        return new TileDefinition('F', "Floor", true, false, false, "floor", "dummy.png", "floor");
    }

    private static TileDefinition doorDefClosed() {
        return new TileDefinition('D', "Door", false, true, false, "cube", "dummy.png", "door");
    }

    @Test
    void tileDefaultsMirrorDefinition() {
        Tile t = new Tile(floorDef());

        assertTrue(t.isWalkable());
        assertFalse(t.isOpenable());
        assertFalse(t.isEndsLevel());
        assertFalse(t.isOpen());
        assertFalse(t.hasItem());
        assertNull(t.getItemId());
    }

    @Test
    void setItemSetsHasItemAndId() {
        Tile t = new Tile(floorDef());

        t.setItem("key");

        assertTrue(t.hasItem());
        assertEquals("key", t.getItemId());
    }

    @Test
    void clearItemResetsState() {
        Tile t = new Tile(floorDef());
        t.setItem("key");

        t.clearItem();

        assertFalse(t.hasItem());
        assertNull(t.getItemId());
    }

    @Test
    void doorOpenAndWalkableAreIndependentFlags() {
        Tile t = new Tile(doorDefClosed());

        t.setOpen(true);

        assertTrue(t.isOpen());
        assertFalse(t.isWalkable());

        t.setWalkable(true);
        assertTrue(t.isWalkable());
    }
}
