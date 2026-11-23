package game.logic.objective;
import org.json.JSONObject;
public interface IConditionFactory {
    String id();
    ICondition create(JSONObject cfg);
}