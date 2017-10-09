package io.github.jbalancer;

import io.github.jbalancer.node.BalancedNode;

import java.util.List;
import java.util.function.Predicate;

/**
 * Main component for performing load balancing.
 */
public interface Balancer {

    /**
     * Returns unique identifier of balancer instance.
     *
     * @return unique identifier as {@link String}
     */
    String getId();

    /**
     * Returns list of all known balanced nodes.
     *
     * @return list of all {@link BalancedNode}
     */
    List<BalancedNode> getAll();

    /**
     * Returns next balanced node by configured balancing strategy.
     *
     * @return next {@link BalancedNode}
     */
    BalancedNode getBalanced();

    /**
     * Returns next balanced node by configured balancing strategy and provided selector as predicate.
     *
     * @return next {@link BalancedNode}
     */
    BalancedNode getBalanced(Predicate<BalancedNode> selector);

}
