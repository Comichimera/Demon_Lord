package game.logic;
import java.util.ArrayList; import java.util.List;
public final class ObjectiveManager implements EventListener {
    private final List<Objective> list = new ArrayList<>();
    public void add(Objective o){ list.add(o); }
    public void attach(RuntimeContext ctx){
        for (Objective o : list) o.condition.attach(ctx);
    }
    @Override public void onEvent(GameEvent e){
        for (Objective o : list) o.condition.onEvent(e);
    }
    public boolean allMandatoryComplete(){
        for (Objective o : list) if (o.mandatory && !o.condition.isTrue()) return false;
        return true;
    }
    public String statusLine(){
        int done = 0; for (Objective o: list) if (o.condition.isTrue()) done++;
        return done + "/" + list.size() + " objectives";
    }
}