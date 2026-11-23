package game.logic.objective;

public final class Objective {
    public final String id;
    public final boolean mandatory;
    public final ICondition condition;
    public final String uiText;
    public Objective(String id, boolean mandatory, ICondition condition, String uiText){
        this.id = id; this.mandatory = mandatory; this.condition = condition; this.uiText = uiText;
    }
}