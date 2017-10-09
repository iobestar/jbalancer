package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;

import java.net.URI;

/**
 *  Checks and updates {@link Node} state based node status URI schema.
 *
 *  If URI schema is {@code tcp} then {@link TcpChecker} is used.
 *  If URI schema is {@code http} or {@code https} then {@link HttpChecker} is used.
 */
public class SchemaAwareChecker implements Checker {

    private final TcpChecker tcpChecker;
    private final HttpChecker httpChecker;

    SchemaAwareChecker(TcpChecker tcpChecker, HttpChecker httpChecker) {
        this.tcpChecker = tcpChecker;
        this.httpChecker = httpChecker;
    }

    @Override
    public void check(Node node) {

        URI status = node.getStatus();
        if (null == status) {
            status = node.getConnection();
        }

        if (null == status) return;

        String scheme = status.getScheme();
        if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
            httpChecker.check(node);
            return;
        }

        if (scheme.equalsIgnoreCase("tcp")) {
            tcpChecker.check(node);
        }
    }
}
