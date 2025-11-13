package game.logic;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ObjectiveManager implements EventListener {
    private final List<Objective> list = new ArrayList<>();

    private final List<Boolean> wasComplete = new ArrayList<>();

    private final Deque<String> popupQueue = new ArrayDeque<>();

    public void add(Objective o){
        list.add(o);
        wasComplete.add(false); // initialize
    }

    public void attach(RuntimeContext ctx){
        for (Objective o : list) o.condition.attach(ctx);
        // Prime states after attach
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

    public String statusLine(){
        int done = 0; for (Objective o: list) if (o.condition.isTrue()) done++;
        return done + "/" + list.size() + " objectives";
    }

    public java.util.List<String> drainCompletedPopups() {
        java.util.List<String> out = new java.util.ArrayList<>(popupQueue);
        popupQueue.clear();
        return out;
    }

    private String completionText(Objective o) {
        // e.g., "Objective complete: Reach the control room"
        return "Objective complete: " + (o.uiText != null ? o.uiText : o.id);
    }
}