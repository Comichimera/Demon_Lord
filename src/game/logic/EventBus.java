package game.logic;
import java.util.ArrayList; import java.util.List;
public final class EventBus {
    private final List<EventListener> listeners = new ArrayList<>();
    public void subscribe(EventListener l){ listeners.add(l); }
    public void post(GameEvent e){ for (EventListener l: listeners) l.onEvent(e); }
}