package game.logic.objective.cond;

import game.logic.objective.ICondition;
import game.logic.event.GameEvent;
import game.logic.event.EGameEventType;
import game.logic.objective.IRuntimeContext;

public final class ReachCell implements ICondition {
    private final int tx, ty;
    private boolean reached = false;
    public ReachCell(int tx, int ty){ this.tx = tx; this.ty = ty; }
    @Override public void attach(IRuntimeContext ctx){ }
    @Override public void onEvent(GameEvent e){
        if (e.type == EGameEventType.ENTER_TILE && e.x == tx && e.y == ty) reached = true;
    }
    @Override public boolean isTrue(){ return reached; }
}