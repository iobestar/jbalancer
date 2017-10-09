package io.github.jbalancer;

import io.github.jbalancer.node.BalancedNode;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.node.checker.Checker;
import io.github.jbalancer.node.discoverer.Discoverer;
import io.github.jbalancer.strategy.Strategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultBalancerTest {

    @Mock
    private Strategy strategy;

    @Mock
    private Discoverer discoverer;

    @Mock
    private Checker checker;

    private DefaultBalancer balancer;

    private static Node createNode(String connection) {
        return new Node(URI.create(connection), URI.create(connection));
    }

    private static Node createDisabledNode(String connection) {

        Node node = createNode(connection);
        node.disable();
        return node;
    }

    private static Node createEnabledNode(String connection) {

        Node node = createNode(connection);
        node.enable();
        return node;
    }

    @Test
    public void createBalancerIdIfNotDefined() throws Exception {

        givenDefaultBalancerWithoutDiscoverer(null, Collections.emptyList());

        assertThat(balancer.getId()).isNotNull();
    }

    @Test
    public void createBalancerWithId() throws Exception {

        givenDefaultBalancerWithoutDiscoverer("balancerid", Collections.emptyList());

        assertThat(balancer.getId()).isEqualTo("balancerid");
    }

    @Test
    public void returnAllInitialNodes() throws Exception {

        givenDefaultBalancerWithoutDiscoverer(null, Arrays.asList(createNode("http://local:10"),
                createNode("http://local:11")));

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:10"), URI.create("http://local:11"));
    }

    @Test
    public void returnAllDiscoveredNodes() throws Exception {

        when(discoverer.discover(anyString())).thenReturn(Arrays.asList(createNode("http://local:10"),
                createNode("http://local:11")));
        givenDefaultBalancerWithDiscoverer(null, Collections.emptyList());

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:10"), URI.create("http://local:11"));
    }

    @Test
    public void getBalancedNode() throws Exception {

        Node node1 = createNode("http://local:10");
        when(strategy.balance(any(Balancer.class))).thenReturn(node1);
        givenDefaultBalancer();

        assertThat(balancer.getBalanced()).isEqualTo(node1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBalancedNodeWithSelector() throws Exception {

        Node node1 = createNode("http://local:10");
        when(strategy.balance(any(Balancer.class), any(Predicate.class))).thenReturn(node1);
        givenDefaultBalancer();

        assertThat(balancer.getBalanced(e -> true)).isEqualTo(node1);
    }

    @Test
    public void discoverNodesWithDiscoverer() throws Exception {

        givenDefaultBalancerWithDiscoverer(null, Collections.emptyList());
        reset(discoverer);
        reset(checker);

        assertThat(balancer.getAll()).isEmpty();

        when(discoverer.discover(anyString())).thenReturn(Collections.singletonList(createNode("http://local:10")));

        balancer.discover();

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:10"));
        verify(checker, times(1)).check(any(Node.class));
    }

    @Test
    public void discoverInitialNodesWhenDiscovererReturnsEmptyList() throws Exception {

        List<Node> initialNodes = Collections.singletonList(createNode("http://local:42"));

        givenDefaultBalancerWithDiscoverer(null, initialNodes);
        reset(discoverer);
        reset(checker);

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:42"));

        when(discoverer.discover(anyString()))
                .thenReturn(Collections.singletonList(createNode("http://discovered:42")))
                .thenReturn(Collections.emptyList());

        balancer.discover();

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://discovered:42"));

        balancer.discover();

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:42"));

        verify(checker, times(2)).check(any(Node.class));
    }

    @Test
    public void discoverInitialNodesWhenDiscovererThrowsException() throws Exception {

        List<Node> initialNodes = Collections.singletonList(createNode("http://local:42"));

        givenDefaultBalancerWithDiscoverer(null, initialNodes);
        reset(discoverer);
        reset(checker);

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:42"));

        when(discoverer.discover(anyString()))
                .thenReturn(Collections.singletonList(createNode("http://discovered:42")))
                .thenThrow(RuntimeException.class);

        balancer.discover();

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://discovered:42"));

        balancer.discover();

        assertThat(balancer.getAll()).extracting(BalancedNode::getConnection).contains(URI.create("http://local:42"));

        verify(checker, times(2)).check(any(Node.class));
    }

    @Test
    public void noCheckerActionsWhenDiscovererReturnsNull() throws Exception {

        givenDefaultBalancerWithDiscoverer(null, Collections.emptyList());
        reset(discoverer);
        reset(checker);

        when(discoverer.discover(anyString())).thenReturn(null);

        verify(checker, never()).check(any(Node.class));
    }

    @Test
    public void checkNodes() throws Exception {

        givenDefaultBalancerWithDiscoverer(null, Collections.singletonList(createNode("http://local:42")));
        reset(checker);

        balancer.check();

        verify(checker, times(1)).check(any(Node.class));
    }

    @Test
    public void enableNode() throws Exception {

        givenDefaultBalancerWithDiscoverer(null, Collections.singletonList(createDisabledNode("http://local:42")));

        balancer.enable(e -> true);

        assertThat(balancer.getAll().get(0).isEnabled()).isTrue();
    }

    @Test
    public void disableNode() throws Exception {

        givenDefaultBalancerWithDiscoverer(null, Collections.singletonList(createEnabledNode("http://local:42")));

        balancer.disable(e -> true);

        assertThat(balancer.getAll().get(0).isEnabled()).isFalse();
    }

    private void givenDefaultBalancerWithDiscoverer(String id, List<Node> nodes) {

        balancer = new DefaultBalancer(id, strategy, discoverer, checker, nodes);
    }

    private void givenDefaultBalancerWithoutDiscoverer(String id, List<Node> nodes) {

        balancer = new DefaultBalancer(id, strategy, null, checker, nodes);
    }

    private void givenDefaultBalancer() {

        balancer = new DefaultBalancer(null, strategy, discoverer, checker, Collections.emptyList());
    }
}