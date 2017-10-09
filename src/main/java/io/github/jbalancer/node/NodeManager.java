package io.github.jbalancer.node;

import java.util.function.Predicate;

public interface NodeManager {

    /**
     * Enables all {@link BalancedNode} selected with selector predicate.
     *
     * @param selector node selector predicate
     */
    void enable(Predicate<BalancedNode> selector);

    /**
     * Disables all {@link BalancedNode} selected with selector predicate.
     *
     * @param selector node selector predicate
     */
    void disable(Predicate<BalancedNode> selector);
}
