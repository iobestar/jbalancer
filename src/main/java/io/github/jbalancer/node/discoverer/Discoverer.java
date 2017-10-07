package io.github.jbalancer.node.discoverer;

import io.github.jbalancer.node.Node;

import java.util.List;

public interface Discoverer {

    List<Node> discover(String balancerId);
}
