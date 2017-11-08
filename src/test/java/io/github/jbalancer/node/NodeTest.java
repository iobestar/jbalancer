package io.github.jbalancer.node;

import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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

        Node node = createNode();
        assertThat(node.isAlive()).isFalse();

        node.setAlive(true);
        assertThat(node.isAlive()).isTrue();

        node.setAlive(false);
        assertThat(node.isAlive()).isFalse();
    }

    @Test
    public void activateDeactivateNode() throws Exception {

        Node node = createNode();
        assertThat(node.isActive()).isFalse();

        node.setActive(true);
        assertThat(node.isActive()).isTrue();

        node.setActive(false);
        assertThat(node.isActive()).isFalse();
    }

    @Test
    public void enableDisableNode() throws Exception {

        Node node = createNode();
        assertThat(node.isEnabled()).isTrue();

        node.disable();
        assertThat(node.isEnabled()).isFalse();

        node.enable();
        assertThat(node.isEnabled()).isTrue();
    }

    @Test
    public void setActiveAndGetPrevious() throws Exception {

        Node node = createNode();
        final boolean current = node.isActive();

        assertThat(node.isActive() == node.setActiveAndGetPrevious(!current));
    }

    @Test
    public void setAliveAndGetPrevious() throws Exception {

        Node node = createNode();
        final boolean current = node.isAlive();

        assertThat(node.isAlive() == node.setAliveAndGetPrevious(!current));
    }

    private static Node createNode() {
        return new Node(Collections.emptyMap(),URI.create("http://localhost:9090"), URI.create("http://localhost:9090/status"));
    }
}