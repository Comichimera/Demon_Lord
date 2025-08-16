package game.logic;
public final class Objective {
    public final String id;
    public final boolean mandatory;
    public final Condition condition;
    public final String uiText;
    public Objective(String id, boolean mandatory, Condition condition, String uiText){
        this.id = id; this.mandatory = mandatory; this.condition = condition; this.uiText = uiText;
    }
}