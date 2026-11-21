package game.inventory;

import java.util.Collections;
import java.util.List;

public final class ItemDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final String iconPath;
    private final boolean stackable;
    private final int maxStack;
    private final List<ItemEffect> effects;

    public ItemDefinition(String id,
                          String name,
                          String description,
                          String iconPath,
                          boolean stackable,
                          int maxStack) {
        this(id, name, description, iconPath, stackable, maxStack, Collections.emptyList());
    }

    public ItemDefinition(String id,
                          String name,
                          String description,
                          String iconPath,
                          boolean stackable,
                          int maxStack,
                          List<ItemEffect> effects) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Item id must not be empty");
        }
        if (stackable && maxStack <= 0) {
            throw new IllegalArgumentException("Stackable items must have maxStack > 0");
        }

        this.id = id;
        this.name = name != null ? name : id;
        this.description = description != null ? description : "";
        this.iconPath = iconPath;
        this.stackable = stackable;
        this.maxStack = stackable ? maxStack : 1;
        this.effects = effects != null ? Collections.unmodifiableList(effects) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }

    public boolean isStackable() {
        return stackable;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }
}
