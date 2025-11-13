// game/logic/GameEvent.java
package game.logic;
public final class GameEvent {
    public final GameEventType type;
    public final int x, y;
    public GameEvent(GameEventType type, int x, int y) {
        this.type = type; this.x = x; this.y = y;
    }
    public static GameEvent simple(GameEventType t){ return new GameEvent(t,-1,-1); }
}