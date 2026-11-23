package game.logic.event;

import java.util.ArrayList;
import java.util.List;

public final class EventBus {
    private final List<IEventListener> listeners = new ArrayList<>();
    public void subscribe(IEventListener l){ listeners.add(l); }
    public void post(GameEvent e){ for (IEventListener l: listeners) l.onEvent(e); }
}