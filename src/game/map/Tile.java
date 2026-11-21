package game.map;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private final TileDefinition def;
    private boolean walkable;
    private boolean endsLevel;
    private boolean openable;
    private boolean open;
    private boolean locked;
    private List<String> lockedByKeys;

    private String itemId;
    private boolean hasItem;

    public Tile(TileDefinition def) {
        this.def          = def;
        this.walkable     = def.isWalkable();
        this.openable     = def.isOpenable();
        this.endsLevel    = def.isEndsLevel();
        this.open         = false;
        this.locked       = false;
        this.lockedByKeys = new ArrayList<>();
        this.itemId       = null;
        this.hasItem      = false;
    }

    public char getType() {
        return def.getId();
    }

    public boolean isWalkable()   { return walkable;  }
    public void    setWalkable(boolean w)   { walkable   = w; }

    public boolean isEndsLevel()  { return endsLevel; }
    public void    setEndsLevel(boolean e)  { endsLevel  = e; }

    public boolean isOpenable()   { return openable;  }
    public void    setOpenable(boolean o)  { openable   = o; }

    public boolean isOpen()       { return open; }
    public void    setOpen(boolean o)      { open = o; }

    public boolean isLocked()  { return locked; }
    public void    setLocked(boolean l)  { locked = l; }

    public List<String> getLockedByKeys() { return lockedByKeys; }
    public void         setLockedByKeys(List<String> keys) { lockedByKeys = keys; }

    public boolean hasItem() {
        return hasItem;
    }

    public String getItemId() {
        return itemId;
    }
    public void setItem(String itemId) {
        this.itemId = itemId;
        this.hasItem = (itemId != null);
    }
    public void clearItem() {
        this.itemId = null;
        this.hasItem = false;
    }
    public TileDefinition getDefinition() {
        return def;
    }
}
