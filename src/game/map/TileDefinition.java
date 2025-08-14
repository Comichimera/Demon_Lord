package game.map;

public class TileDefinition {
    private final char id;
    private final String name;
    private final boolean walkable;
    private final boolean openable;
    private final boolean endsLevel;
    private final String renderer;
    private final String texture;
    private final String category;

    public TileDefinition(char id, String name, boolean walkable, boolean openable,
                          boolean endsLevel, String renderer, String texture,
                          String category) {
        this.id = id; this.name = name; this.walkable = walkable;
        this.openable = openable; this.endsLevel = endsLevel;
        this.renderer = renderer; this.texture = texture;
        this.category = category;
    }

    public char getId()           { return id; }
    public String getName()       { return name; }
    public boolean isWalkable()   { return walkable; }
    public boolean isOpenable()   { return openable; }
    public boolean isEndsLevel()  { return endsLevel; }
    public String getRenderer()   { return renderer; }
    public String getTexture()    { return texture; }
    public String getCategory() { return category; }
}
