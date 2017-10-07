package io.github.jbalancer;

import io.github.jbalancer.node.BalancedNode;

import java.util.List;
import java.util.function.Predicate;

public interface Balancer {

    String getId();

    List<BalancedNode> getAll();

    BalancedNode getBalanced();

    BalancedNode getBalanced(Predicate<BalancedNode> selector);

}
