package game.ai.bt;

import game.ai.AIAgent;
import game.ai.Action;
import game.ai.Blackboard;

import java.util.Arrays;
import java.util.List;

public final class Parallel implements Node {
    public enum Policy { REQUIRE_ONE, REQUIRE_ALL }

    private final List<Node> children;
    private final Policy successPolicy;
    private final Policy failurePolicy;
    private final Action.Status[] lastStatus;

    public Parallel(List<Node> children, Policy successPolicy, Policy failurePolicy) {
        this.children = children;
        this.successPolicy = successPolicy;
        this.failurePolicy = failurePolicy;
        this.lastStatus = new Action.Status[children.size()];
        Arrays.fill(lastStatus, Action.Status.RUNNING);
    }

    @Override public Action.Status tick(float dt, AIAgent agent, Blackboard bb) {
        int successes = 0, failures = 0;
        for (int i = 0; i < children.size(); i++) {
            if (lastStatus[i] == Action.Status.RUNNING) {
                lastStatus[i] = children.get(i).tick(dt, agent, bb);
            }
            if (lastStatus[i] == Action.Status.SUCCESS) successes++;
            if (lastStatus[i] == Action.Status.FAILURE)  failures++;
        }

        boolean succeeded = (successPolicy == Policy.REQUIRE_ONE && successes > 0)
                         || (successPolicy == Policy.REQUIRE_ALL && successes == children.size());
        boolean failed    = (failurePolicy == Policy.REQUIRE_ONE && failures > 0)
                         || (failurePolicy == Policy.REQUIRE_ALL && failures == children.size());

        if (succeeded || failed) {
            cleanupRunning(agent, bb);
            return succeeded ? Action.Status.SUCCESS : Action.Status.FAILURE;
        }
        return Action.Status.RUNNING;
    }

    @Override public void onExit(AIAgent agent, Blackboard bb) {
        cleanupRunning(agent, bb);
    }

    private void cleanupRunning(AIAgent agent, Blackboard bb) {
        for (int i = 0; i < children.size(); i++) {
            if (lastStatus[i] == Action.Status.RUNNING) {
                children.get(i).onExit(agent, bb);
            }
        }
        Arrays.fill(lastStatus, Action.Status.RUNNING);
    }
}
