package game.logic.cond;
import game.logic.*;
public final class ReachCell implements Condition {
    private final int tx, ty;
    private boolean reached = false;
    public ReachCell(int tx, int ty){ this.tx = tx; this.ty = ty; }
    @Override public void attach(RuntimeContext ctx){ /* nothing */ }
    @Override public void onEvent(GameEvent e){
        if (e.type == GameEventType.ENTER_TILE && e.x == tx && e.y == ty) reached = true;
    }
    @Override public boolean isTrue(){ return reached; }
}