package io.github.jbalancer.node;

import java.util.function.Predicate;

public interface NodeManager {

    void enable(Predicate<BalancedNode> selector);

    void disable(Predicate<BalancedNode> selector);
}
