package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;

public interface Checker {

    /**
     * Checks and updates state of node. Node state includes aliveness and activeness.
     * Node is alive is is reachable. Node is active if is ready to receive requests.
     *
     * @param node instance of {@link Node}
     */
    void check(Node node);
}
