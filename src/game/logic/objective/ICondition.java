package game.logic.objective;

import game.logic.event.IEventListener;

public interface ICondition extends IEventListener {
    void attach(IRuntimeContext ctx);
    boolean isTrue();
}