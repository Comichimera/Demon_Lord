package game.logic;
import org.json.JSONObject;
public interface ConditionFactory {
    String id();
    Condition create(JSONObject cfg);
}