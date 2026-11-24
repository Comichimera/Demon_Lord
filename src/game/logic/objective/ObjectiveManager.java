package game.logic.objective;

import game.logic.event.IEventListener;
import game.logic.event.GameEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ObjectiveManager implements IEventListener {
    private final List<Objective> list = new ArrayList<>();

    private final List<Boolean> wasComplete = new ArrayList<>();

    private final Deque<String> popupQueue = new ArrayDeque<>();

    public void add(Objective o){
        list.add(o);
        wasComplete.add(false);
    }

    public void attach(IRuntimeContext ctx){
        for (Objective o : list) o.condition.attach(ctx);
        for (int i = 0; i < list.size(); i++) {
            boolean now = list.get(i).condition.isTrue();
            wasComplete.set(i, now);
        }
    }

    @Override
    public void onEvent(GameEvent e){
        for (int i = 0; i < list.size(); i++) {
            Objective o = list.get(i);
            o.condition.onEvent(e);
            boolean now = o.condition.isTrue();
            if (!wasComplete.get(i) && now) {
                popupQueue.addLast(completionText(o));
            }
            wasComplete.set(i, now);
        }
    }

    public boolean allMandatoryComplete(){
        for (Objective o : list) if (o.mandatory && !o.condition.isTrue()) return false;
        return true;
    }

    public List<String> drainCompletedPopups() {
        List<String> out = new ArrayList<>(popupQueue);
        popupQueue.clear();
        return out;
    }

    private String completionText(Objective o) {
        return "Objective complete: " + (o.uiText != null ? o.uiText : o.id);
    }
}