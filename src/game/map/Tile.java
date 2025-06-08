package game.map;

public class Tile {
    private final TileDefinition def;
    private boolean walkable;
    private boolean endsLevel;
    private boolean openable;
    private boolean open;

    public Tile(TileDefinition def) {
        this.def       = def;
        this.walkable  = def.isWalkable();
        this.openable  = def.isOpenable();
        this.endsLevel = def.isEndsLevel();
        this.open      = false;
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

    public TileDefinition getDefinition() {
        return def;
    }
}
