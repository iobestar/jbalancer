package io.github.jbalancer.node;

import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NodeTest {

    @Test
    public void labelsAreNotNull() throws Exception {

        assertThat(new Node(URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status")).getLabels()).isNotNull();
    }

    @Test
    public void nodeIsNotActiveByDefault() throws Exception {

        assertThat(new Node(URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status")).isActive()).isFalse();
    }

    @Test
    public void nodeIsEnabledByDefault() throws Exception {

        assertThat(new Node(URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status")).isEnabled()).isTrue();
    }

    @Test
    public void markNodeAliveDead() throws Exception {

        int stateBarrier = 3;

        Node node = createNodeWithStateBarrier(stateBarrier);
        assertThat(node.isAlive()).isFalse();

        node.setAlive(true);
        assertThat(node.isAlive()).isTrue();

        for (int i = 0; i < stateBarrier; i++) {
            node.setAlive(false);
        }
        assertThat(node.isAlive()).isFalse();
    }

    @Test
    public void activateDeactivateNode() throws Exception {

        int stateBarrier = 3;

        Node node = createNodeWithStateBarrier(3);
        assertThat(node.isActive()).isFalse();

        node.setActive(true);
        assertThat(node.isActive()).isTrue();

        for (int i = 0; i < stateBarrier; i++) {
            node.setActive(false);
        }
        assertThat(node.isActive()).isFalse();
    }

    @Test
    public void enableDisableNode() throws Exception {

        Node node = new Node(URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status"));
        assertThat(node.isEnabled()).isTrue();

        node.disable();
        assertThat(node.isEnabled()).isFalse();

        node.enable();
        assertThat(node.isEnabled()).isTrue();
    }

    @Test
    public void invalidStateBarrier() throws Exception {

        assertThatThrownBy(() -> createNodeWithStateBarrier(0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Node createNodeWithStateBarrier(int stateBarrier) {
        return new Node(Collections.emptyMap(),URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status"), stateBarrier);
    }
}