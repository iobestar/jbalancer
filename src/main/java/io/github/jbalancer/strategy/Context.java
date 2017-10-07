package io.github.jbalancer.strategy;

import io.github.jbalancer.node.BalancedNode;

import java.util.function.Predicate;

/**
 * Strategy context wrapper. Contains contextual data for performing balancing strategy.
 *
 * @see Strategy
 * @see RoundRobinStrategy
 */
public class Context {

    private final Predicate<BalancedNode> selector;

    public Context(Predicate<BalancedNode> selector) {
        this.selector = selector;
    }

    public Predicate<BalancedNode> getSelector() {
        return selector;
    }
}
