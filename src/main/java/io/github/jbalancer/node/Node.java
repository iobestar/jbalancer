package io.github.jbalancer.node;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represent one network node identified by connection {@link URI} and status {@link URI}.
 */
public class Node implements BalancedNode {

    /**
     * Node aliveness - true or false.
     */
    private final AtomicBoolean alive = new AtomicBoolean(false);

    /**
     * Node activeness - true or false.
     */
    private final AtomicBoolean active = new AtomicBoolean(false);

    /**
     * Node availability - {@code true} if node can become active {@code false} if can't.
     */
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    /**
     * Node connection {@link URI}. {@link URI} for client connect.
     */
    private final URI connection;

    /**
     * Node status {@link URI}. {@link URI} for status check.
     */
    private final URI status;

    /**
     * Set of labels for additional node description.
     */
    private Map<String, String> labels;

    /**
     * Check status message.
     */
    private volatile String checkStatus;

    public Node(URI connection, URI status) {
        this(Collections.emptyMap(), connection, status);
    }

    public Node(Map<String, String> labels, URI connection, URI status) {
        this.labels = Optional.ofNullable(labels).orElseGet(Collections::emptyMap);
        this.connection = connection;
        this.status = status;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public boolean isAlive() {
        return alive.get();
    }

    public void setAlive(boolean alive) {
        this.alive.compareAndSet(!alive, alive);
    }

    public boolean setAliveAndGetPrevious(boolean alive) {

        final boolean previous = this.alive.get();
        setAlive(alive);
        return previous;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.compareAndSet(!active, active);
    }

    public boolean setActiveAndGetPrevious(boolean active) {

        final boolean previous = this.active.get();
        setActive(active);
        return previous;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void enable() {
        this.enabled.compareAndSet(false, true);
    }

    public void disable() {
        this.enabled.compareAndSet(true, false);
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    @Override
    public URI getConnection() {
        return connection;
    }

    @Override
    public URI getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(labels, node.labels) &&
                Objects.equals(connection, node.connection) &&
                Objects.equals(status, node.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, connection, status);
    }

    @Override
    public String toString() {
        return "Node{" +
                "connection=" + connection +
                ", status=" + status +
                ", labels=" + labels +
                '}';
    }
}
