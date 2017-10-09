package io.github.jbalancer.balancing;

import io.github.jbalancer.Balancer;
import io.github.jbalancer.node.BalancedNode;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.strategy.Context;
import io.github.jbalancer.strategy.RoundRobinStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoundRobinStrategyTest {


    @Mock
    private Context context;

    @Mock
    private Balancer balancer;

    private RoundRobinStrategy roundRobinBalancing = new RoundRobinStrategy();

    private static Node createNode(String cnnPoint, boolean active, boolean enabled) {

        Node node = new Node(URI.create(cnnPoint), URI.create(cnnPoint));
        node.setActive(active);
        if (enabled) node.enable();
        if (!enabled) node.disable();
        return node;
    }

    @Test
    public void balanceNullWhenNoNodes() throws Exception {

        when(balancer.getAll()).thenReturn(Collections.emptyList());

        assertThat(roundRobinBalancing.balance(balancer, context)).isNull();
    }

    @Test
    public void balanceActiveAndEnabled() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", false, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", true, false);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        assertThat(roundRobinBalancing.balance(balancer, context)).extracting(BalancedNode::getConnection)
                .containsOnly(URI.create("http://localhost:9090/second"));
    }

    @Test
    public void balanceActiveAndEnabledWhenContextIsNull() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", false, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", true, false);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        assertThat(roundRobinBalancing.balance(balancer, (Context) null)).extracting(BalancedNode::getConnection)
                .containsOnly(URI.create("http://localhost:9090/second"));
    }

    @Test
    public void balanceNullWhenAllNodesAreInactive() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", false, true);
        Node node2 = createNode("http://localhost:9090/second", false, false);
        Node node3 = createNode("http://localhost:9090/third", false, true);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        assertThat(roundRobinBalancing.balance(balancer, context)).isNull();
    }

    @Test
    public void balanceActiveAndEnabledInRound() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", true, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", true, true);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        List<BalancedNode> balancedNodes = Arrays.asList(
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context)
        );

        assertThat(balancedNodes).extracting(BalancedNode::getConnection).containsExactly(
                URI.create("http://localhost:9090/first"),
                URI.create("http://localhost:9090/second"),
                URI.create("http://localhost:9090/third"),
                URI.create("http://localhost:9090/first")
        );
    }

    @Test
    public void balanceFirstActiveAndEnabled() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", false, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", false, true);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        List<BalancedNode> balancedNodes = Arrays.asList(
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context)
        );

        assertThat(balancedNodes).extracting(BalancedNode::getConnection).containsExactly(
                URI.create("http://localhost:9090/second"),
                URI.create("http://localhost:9090/second"),
                URI.create("http://localhost:9090/second"),
                URI.create("http://localhost:9090/second")
        );
    }

    @Test
    public void balanceOnlyActiveAndEnabledInRound() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", true, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", false, true);

        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));

        List<BalancedNode> balancedNodes = Arrays.asList(
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context)
        );
        assertThat(balancedNodes).extracting(BalancedNode::getConnection).containsExactly(
                URI.create("http://localhost:9090/first"),
                URI.create("http://localhost:9090/second"),
                URI.create("http://localhost:9090/first"),
                URI.create("http://localhost:9090/second")
        );
    }

    @Test
    public void balanceWithSelectorInRound() throws Exception {

        Node node1 = createNode("http://localhost:9090/first", true, true);
        Node node2 = createNode("http://localhost:9090/second", true, true);
        Node node3 = createNode("http://localhost:9090/third", true, true);
        when(balancer.getAll()).thenReturn(Arrays.asList(node1, node2, node3));
        when(context.getSelector()).thenReturn(node -> node.getConnection().getPath().equals("/first"));

        List<BalancedNode> balancedNodes = Arrays.asList(
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context),
                roundRobinBalancing.balance(balancer, context)
        );

        assertThat(balancedNodes).extracting(BalancedNode::getConnection).containsExactly(
                URI.create("http://localhost:9090/first"),
                URI.create("http://localhost:9090/first"),
                URI.create("http://localhost:9090/first")
        );
    }
}