package io.github.jbalancer;

import io.github.jbalancer.node.Node;
import io.github.jbalancer.node.discoverer.Discoverer;
import io.github.jbalancer.strategy.Strategy;

import java.util.List;

/**
 * Component for creating {@link Balancer} instances.
 */
public interface BalancerFactory {

    Balancer create(String id, Strategy strategy, Discoverer discoverer);

    Balancer create(String id, Strategy strategy, List<Node> nodes);

    default Balancer create(Strategy strategy, Discoverer discoverer) {
        return create(null, strategy, discoverer);
    }

    default Balancer create(Strategy strategy, List<Node> nodes) {
        return create(null, strategy, nodes);
    }
}
