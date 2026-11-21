package game.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Inventory {

    private static final int MAX_SLOTS = 8;

    private final List<InventoryEntry> entries = new ArrayList<>();
    private int selectedSlot = 0;

    public boolean addItem(String itemId, int amount) {
        if (amount <= 0) return true;
        ItemDefinition def = ItemRegistry.get(itemId);
        if (def == null) {
            System.err.println("Inventory, unknown item id: " + itemId);
            return false;
        }
        return addItem(def, amount);
    }

    public boolean addItem(ItemDefinition def, int amount) {
        if (amount <= 0) return true;

        int remaining = amount;

        if (def.isStackable()) {
            for (InventoryEntry e : entries) {
                if (e.getDefinition().getId().equals(def.getId())) {
                    int space = def.getMaxStack() - e.getCount();
                    if (space <= 0) continue;

                    int toAdd = Math.min(space, remaining);
                    if (toAdd > 0) {
                        e.increment(toAdd);
                        remaining -= toAdd;
                        if (remaining == 0) {
                            return true;
                        }
                    }
                }
            }
        }

        while (remaining > 0 && entries.size() < MAX_SLOTS) {
            int toAdd;
            if (def.isStackable()) {
                toAdd = Math.min(def.getMaxStack(), remaining);
            } else {
                toAdd = 1;
            }
            entries.add(new InventoryEntry(def, toAdd));
            remaining -= toAdd;
        }

        return remaining == 0;
    }

    public boolean removeItem(String itemId, int amount) {
        if (amount <= 0) return true;
        int remaining = amount;

        for (int i = entries.size() - 1; i >= 0; i--) {
            InventoryEntry e = entries.get(i);
            if (!e.getDefinition().getId().equals(itemId)) continue;

            int have = e.getCount();
            int take = Math.min(have, remaining);
            int leftover = have - take;

            if (leftover > 0) {
                e.setCount(leftover);
            } else {
                entries.remove(i);
            }

            remaining -= take;
            if (remaining == 0) break;
        }

        if (selectedSlot >= entries.size() && selectedSlot > 0) {
            selectedSlot = entries.size() - 1;
        }

        return remaining == 0;
    }

    public int countOf(String itemId) {
        int total = 0;
        for (InventoryEntry e : entries) {
            if (e.getDefinition().getId().equals(itemId)) {
                total += e.getCount();
            }
        }
        return total;
    }

    public boolean hasItem(String itemId) {
        return countOf(itemId) > 0;
    }

    public List<InventoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getMaxSlots() {
        return MAX_SLOTS;
    }

    public int getUsedSlots() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
        selectedSlot = 0;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void selectNext() {
        if (entries.isEmpty()) return;
        selectedSlot = (selectedSlot + 1) % entries.size();
    }

    public InventoryEntry getSelectedEntry() {
        if (entries.isEmpty() || selectedSlot >= entries.size()) return null;
        return entries.get(selectedSlot);
    }
}
