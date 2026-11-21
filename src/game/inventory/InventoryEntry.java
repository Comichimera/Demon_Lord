package game.inventory;

public final class InventoryEntry {

    private final ItemDefinition definition;
    private int count;

    public InventoryEntry(ItemDefinition definition, int count) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        if (!definition.isStackable() && count > 1) {
            throw new IllegalArgumentException("non-stackable items cannot have count > 1");
        }
        if (definition.isStackable() && count > definition.getMaxStack()) {
            throw new IllegalArgumentException("count exceeds maxStack for item " + definition.getId());
        }

        this.definition = definition;
        this.count = count;
    }

    public ItemDefinition getDefinition() {
        return definition;
    }

    public int getCount() {
        return count;
    }

    void increment(int delta) {
        this.count += delta;
    }

    void setCount(int count) {
        this.count = count;
    }
}
