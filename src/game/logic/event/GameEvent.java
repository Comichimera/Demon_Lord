package game.logic.event;

public final class GameEvent {
    public final EGameEventType type;
    public final int x, y;
    public GameEvent(EGameEventType type, int x, int y) {
        this.type = type; this.x = x; this.y = y;
    }
    public static GameEvent simple(EGameEventType t){ return new GameEvent(t,-1,-1); }
}