package io.github.jbalancer;

import io.github.jbalancer.node.BalancedNode;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.node.NodeManager;
import io.github.jbalancer.node.checker.Checker;
import io.github.jbalancer.node.discoverer.Discoverer;
import io.github.jbalancer.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DefaultBalancer implements Balancer, NodeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBalancer.class);

    private final String id;
    private final Strategy strategy;
    private final Discoverer discoverer;
    private final Checker checker;

    private final List<Node> initialNodes;
    private volatile List<Node> nodes;

    DefaultBalancer(Strategy strategy, Discoverer discoverer, Checker checker, List<Node> initialNodes) {
        this(null, strategy, discoverer, checker, initialNodes);
    }

    DefaultBalancer(String id, Strategy strategy, Discoverer discoverer, Checker checker, List<Node> initialNodes) {
        this.id = Optional.ofNullable(id).orElseGet(() -> UUID.randomUUID().toString());
        this.strategy = strategy;
        this.discoverer = discoverer;
        this.checker = checker;
        this.initialNodes = initialNodes;

        discover();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<BalancedNode> getAll() {

        List<Node> allNodes = getAllInternal();
        return allNodes.stream().map(n -> (BalancedNode)n).collect(Collectors.toList());
    }

    private List<Node> getAllInternal() {

        if (!nodes.isEmpty()) {
            return nodes;
        }
        return initialNodes;
    }

    @Override
    public BalancedNode getBalanced() {
        return strategy.balance(this);
    }

    @Override
    public BalancedNode getBalanced(Predicate<BalancedNode> selector) {
        return strategy.balance(this, selector);
    }

    void discover() {

        if (null == discoverer) {
            discoverEmptyNodes();
            return;
        }

        List<Node> discovered = Collections.emptyList();
        try {
            discovered = discoverer.discover(getId());
            if (null == discovered) {
                return;
            }
        } catch (Exception e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.error("Error discovering nodes", e);
            } else {
                LOGGER.error("Error discovering nodes", e.getMessage());
            }
        }

        if (discovered.isEmpty()) {
            discoverEmptyNodes();
            return;
        }

        check(discovered);
        nodes = Collections.unmodifiableList(discovered);
    }

    private void discoverEmptyNodes() {

        if (nodes != initialNodes) {
            check(initialNodes);
            nodes = initialNodes;
        }
    }

    private void check(List<Node> nodes) {

        nodes.forEach(checker::check);
    }

    void check() {

        check(getAllInternal());
    }

    @Override
    public void enable(Predicate<BalancedNode> selector) {

        getAllInternal().stream().filter(selector).forEach(Node::enable);
    }

    @Override
    public void disable(Predicate<BalancedNode> selector) {

        getAllInternal().stream().filter(selector).forEach(Node::disable);
    }
}
