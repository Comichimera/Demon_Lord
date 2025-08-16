package game.logic;
import org.json.JSONObject;
public interface ConditionFactory {
    String id();                      // e.g., "reach"
    Condition create(JSONObject cfg); // builds condition from JSON block
}