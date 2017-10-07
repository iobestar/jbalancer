package io.github.jbalancer.node;

import java.net.URI;
import java.util.Map;

public interface BalancedNode {

    /**
     * Returns custom meta data for node in form of key-value pairs.
     *
     * @return {@link Map} of labels
     */
    Map<String, String> getLabels();

    /**
     * Returns {@code true} if node is active and is safe to use. False otherwise.
     *
     * @return {@code true} or {@code false}
     */
    boolean isActive();

    /**
     * Returns {@code true} if node is enabled and should be used. False otherwise.
     *
     * @return {@code true} or {@code false}
     */
    boolean isEnabled();

    /**
     * Node connection {@link URI} used for connecting to serving endpoint.
     *
     * @return connection {@link URI}
     */
    URI getConnection();

    /**
     * Node status {@link URI} used for checking node availability.
     *
     * @return connection {@link URI}
     */
    URI getStatus();
}
