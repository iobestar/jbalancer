package io.github.jbalancer.proxy.dashboard;

import io.github.jbalancer.Balancer;
import io.github.jbalancer.JBalancer;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.node.discoverer.YamlNodeDiscoverer;
import io.github.jbalancer.proxy.YamlNodesStorage;
import io.github.jbalancer.strategy.RoundRobinStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardController {

    private final JBalancer jBalancer;
    private final YamlNodesStorage yamlNodesStorage;

    @Autowired
    public DashboardController(JBalancer jBalancer, YamlNodesStorage yamlNodesStorage) {
        this.jBalancer = jBalancer;
        this.yamlNodesStorage = yamlNodesStorage;
    }

    public List<ANode> getAllNodes() {

        final List<Balancer> balancers = jBalancer.getBalancers();

        final List<ANode> result = new ArrayList<>();
        balancers.forEach(b -> result.addAll(b.getAll().stream()
                .map(bn -> (Node) bn)
                .map(n -> create(b.getId(), n))
                .collect(Collectors.toList())));
        return result;
    }

    public List<ABalancer> getAllBalancers() {

        final List<Balancer> balancers = jBalancer.getBalancers();

        final List<ABalancer> result = new ArrayList<>();
        balancers.forEach(b -> result.add(new ABalancer(b.getId(), StringUtils.EMPTY)));
        return result;
    }

    public ABalancer save(ABalancer aBalancer) {

        final String balancerId = aBalancer.getId();

        yamlNodesStorage.save(aBalancer.getId(), aBalancer.getYamlNodes());
        final Balancer balancer = jBalancer.getBalancer(balancerId);
        if (null == balancer) {
            jBalancer.create(balancerId, new RoundRobinStrategy(), new YamlNodeDiscoverer(s -> yamlNodesStorage.getYamlNodes(balancerId)));
        }
        return aBalancer;
    }

    public ABalancer getBalancer(String balancerId) {

        final String yamlnodes = yamlNodesStorage.getYamlNodes(balancerId);
        return new ABalancer(balancerId, yamlnodes);
    }

    private ANode create(String balancerId, Node node) {
        return new ANode(
                balancerId,
                node.getLabels(),
                null == node.getConnection() ? "" : node.getConnection().toString(),
                null == node.getStatus() ? "" : node.getStatus().toString(),
                node.isAlive(),
                node.isActive(),
                node.isEnabled(),
                node.getCheckStatus()
        );
    }
}
