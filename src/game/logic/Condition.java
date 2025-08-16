package game.logic;
public interface Condition extends EventListener {
    void attach(RuntimeContext ctx);
    boolean isTrue();
}