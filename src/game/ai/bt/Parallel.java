package game.ai.bt;

import game.ai.IAIAgent;
import game.ai.IAction;
import game.ai.Blackboard;

import java.util.Arrays;
import java.util.List;

public final class Parallel implements INode {
    public enum Policy { REQUIRE_ONE, REQUIRE_ALL }

    private final List<INode> children;
    private final Policy successPolicy;
    private final Policy failurePolicy;
    private final IAction.Status[] lastStatus;

    public Parallel(List<INode> children, Policy successPolicy, Policy failurePolicy) {
        this.children = children;
        this.successPolicy = successPolicy;
        this.failurePolicy = failurePolicy;
        this.lastStatus = new IAction.Status[children.size()];
        Arrays.fill(lastStatus, IAction.Status.RUNNING);
    }

    @Override public IAction.Status tick(float dt, IAIAgent agent, Blackboard bb) {
        int successes = 0, failures = 0;
        for (int i = 0; i < children.size(); i++) {
            if (lastStatus[i] == IAction.Status.RUNNING) {
                lastStatus[i] = children.get(i).tick(dt, agent, bb);
            }
            if (lastStatus[i] == IAction.Status.SUCCESS) successes++;
            if (lastStatus[i] == IAction.Status.FAILURE)  failures++;
        }

        boolean succeeded = (successPolicy == Policy.REQUIRE_ONE && successes > 0)
                         || (successPolicy == Policy.REQUIRE_ALL && successes == children.size());
        boolean failed    = (failurePolicy == Policy.REQUIRE_ONE && failures > 0)
                         || (failurePolicy == Policy.REQUIRE_ALL && failures == children.size());

        if (succeeded || failed) {
            cleanupRunning(agent, bb);
            return succeeded ? IAction.Status.SUCCESS : IAction.Status.FAILURE;
        }
        return IAction.Status.RUNNING;
    }

    @Override public void onExit(IAIAgent agent, Blackboard bb) {
        cleanupRunning(agent, bb);
    }

    private void cleanupRunning(IAIAgent agent, Blackboard bb) {
        for (int i = 0; i < children.size(); i++) {
            if (lastStatus[i] == IAction.Status.RUNNING) {
                children.get(i).onExit(agent, bb);
            }
        }
        Arrays.fill(lastStatus, IAction.Status.RUNNING);
    }
}
