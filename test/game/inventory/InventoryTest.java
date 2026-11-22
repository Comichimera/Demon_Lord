package game.inventory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    @BeforeAll
    static void loadItems() {
        ItemDefinitionLoader.loadGlobal();
    }

    @Test
    void addUnknownItemFails() {
        Inventory inv = new Inventory();

        boolean ok = inv.addItem("definitely_not_real_" + UUID.randomUUID(), 1);

        assertFalse(ok);
        assertEquals(0, inv.getUsedSlots());
        assertEquals(0, inv.getEntries().size());
    }

    @Test
    void addKeyFillsSlotsAtEight() {
        Inventory inv = new Inventory();

        for (int i = 0; i < 8; i++) {
            assertTrue(inv.addItem("key", 1), "Expected adding key #" + (i + 1) + " to succeed");
        }

        assertEquals(8, inv.getUsedSlots());
        assertEquals(8, inv.countOf("key"));
    }

    @Test
    void addKeyNinthFailsAndInventoryStaysFull() {
        Inventory inv = new Inventory();

        for (int i = 0; i < 8; i++) {
            assertTrue(inv.addItem("key", 1));
        }

        boolean ok = inv.addItem("key", 1);

        assertFalse(ok);
        assertEquals(8, inv.getUsedSlots());
        assertEquals(8, inv.countOf("key"));
    }

    @Test
    void removeExactKeyCountEmptiesInventory() {
        Inventory inv = new Inventory();
        inv.addItem("key", 1);
        inv.addItem("key", 1);
        inv.addItem("key", 1);

        boolean ok = inv.removeItem("key", 3);

        assertTrue(ok);
        assertEquals(0, inv.getUsedSlots());
        assertEquals(0, inv.countOf("key"));
        assertFalse(inv.hasItem("key"));
    }

    @Test
    void removeMoreThanHaveReturnsFalseButStillRemovesWhatItCan() {
        Inventory inv = new Inventory();
        inv.addItem("key", 1);
        inv.addItem("key", 1);

        boolean ok = inv.removeItem("key", 3);

        assertFalse(ok);

        assertEquals(0, inv.getUsedSlots());
        assertEquals(0, inv.countOf("key"));
    }

    @Test
    void stackableItemFillsStacksBeforeNewSlots() {
        Inventory inv = new Inventory();

        String id = "test_stack_" + UUID.randomUUID();
        ItemRegistry.register(new ItemDefinition(
                id,
                "Test Stack Item",
                "",
                null,
                true,
                5
        ));

        assertTrue(inv.addItem(id, 7));

        assertEquals(2, inv.getUsedSlots());
        assertEquals(7, inv.countOf(id));

        assertEquals(5, inv.getEntries().get(0).getCount());
        assertEquals(2, inv.getEntries().get(1).getCount());
    }
}
