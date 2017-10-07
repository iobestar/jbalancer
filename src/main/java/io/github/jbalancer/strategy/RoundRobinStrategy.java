package io.github.jbalancer.strategy;

import io.github.jbalancer.Balancer;
import io.github.jbalancer.node.BalancedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simple balancing strategy for evenly distributed node balancing.
 */
public class RoundRobinStrategy implements Strategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinStrategy.class);

    private AtomicInteger counter;

    public RoundRobinStrategy() {
        this.counter = new AtomicInteger(0);
    }

    @Override
    public BalancedNode balance(Balancer balancer, Context context) {

        if (null == balancer) {
            LOGGER.debug("Balancer is null; returned null node");
            return null;
        }

        List<BalancedNode> available = balancer.getAll().stream()
                .filter(getSelector(context))
                .filter(BalancedNode::isEnabled)
                .filter(BalancedNode::isActive)
                .collect(Collectors.toList());

        if (null == available || available.isEmpty()) {
            LOGGER.debug("No available nodes; returned null node");
            return null;
        }

        return available.get((nextIndex(available.size())));
    }

    private Predicate<BalancedNode> getSelector(Context context) {

        if (null == context) {
            return node -> true;
        }

        Predicate<BalancedNode> selector = context.getSelector();
        if (null == selector) {
            return e -> true;
        }
        return selector;
    }

    private int nextIndex(int modulo) {
        return Math.abs(counter.getAndIncrement() % modulo);
    }
}
