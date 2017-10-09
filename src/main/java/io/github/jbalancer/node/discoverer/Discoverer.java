package io.github.jbalancer.node.discoverer;

import io.github.jbalancer.node.Node;

import java.util.List;

public interface Discoverer {

    /**
     * Discovers list of {@link Node} for balancing based on provided balancer id.
     *
     * If method throws exception or returns empty list fallback/initial nodes of balancer are used.
     * Method should return null if no changes discovered.
     *
     * @param balancerId unique identifier of balancer
     * @return list of discovered {@link Node}
     */
    List<Node> discover(String balancerId);
}
