package io.github.jbalancer.strategy;

import io.github.jbalancer.Balancer;
import io.github.jbalancer.node.BalancedNode;

import java.util.function.Predicate;

/**
 * Component for performing balancing strategy.
 *
 * @see RoundRobinStrategy
 */
public interface Strategy {

    /**
     * Using {@code balancer} and {@code context} performs balancing over available nodes.
     *
     * @param balancer instance of {@link Balancer}
     * @param context  instance of balancing context as {@link Context}
     * @return balanced node as {@link BalancedNode} or null if no available node by this strategy
     */
    BalancedNode balance(Balancer balancer, Context context);

    /**
     * Using {@code balancer} and null {@code context} performs balancing over available nodes.
     *
     * @param balancer instance of {@link Balancer}
     * @return balanced node as {@link BalancedNode} or null if no available node by this strategy
     */
    default BalancedNode balance(Balancer balancer) {
        return balance(balancer, (Context) null);
    }

    /**
     * Using {@code balancer} and {@code selector} as predicate performs balancing over available and selected nodes.
     *
     * @param balancer instance of {@link Balancer}
     * @param selector balancer nodes selector
     * @return balanced node as {@link BalancedNode} or null if no available node by this strategy
     */
    default BalancedNode balance(Balancer balancer, Predicate<BalancedNode> selector) {
        return balance(balancer, new Context(selector));
    }
}
