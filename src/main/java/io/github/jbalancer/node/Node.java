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

    private static final int DEFAULT_STATE_BARRIER = 3;

    /**
     * Node aliveness - zero if is reachable non-zero could be unreachable.
     */
    private volatile int aliveCount = 0;

    /**
     * Node activeness - zero if node is ready to serve non-zero could be not ready to serve.
     */
    private volatile int activeCount = 0;


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

    private final int stateBarrier;

    /**
     * Check status message.
     */
    private volatile String checkStatus;

    public Node(URI connection, URI status) {
        this(Collections.emptyMap(), connection, status);
    }

    public Node(Map<String, String> labels, URI connection, URI status) {
        this(labels, connection, status, DEFAULT_STATE_BARRIER);
    }

    public Node(Map<String, String> labels, URI connection, URI status, int stateBarrier) {
        this.labels = Optional.ofNullable(labels).orElseGet(Collections::emptyMap);
        this.connection = connection;
        this.status = status;
        this.stateBarrier = checkStateBarrier(stateBarrier);
    }

    private int checkStateBarrier(int stateBarrier) {

        if (stateBarrier < 1) throw new IllegalArgumentException("State barrier must be at least 1");
        return stateBarrier;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public boolean isAlive() {
        return aliveCount == stateBarrier;
    }

    public void setAlive(boolean alive) {

        if (!alive) {
            synchronized (this) {
                if (aliveCount >= stateBarrier) aliveCount--;
            }
        } else aliveCount = stateBarrier;
    }

    public boolean isActive() {
        return activeCount == stateBarrier;
    }

    public void setActive(boolean active) {

        if (!active) {
            synchronized (this) {
                if (activeCount >= stateBarrier) activeCount--;
            }
        } else activeCount = stateBarrier;
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
