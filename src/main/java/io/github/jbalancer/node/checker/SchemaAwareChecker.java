package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;

import java.net.URI;

public class SchemaAwareChecker implements Checker {

    private final TcpChecker tcpChecker;
    private final HttpChecker httpChecker;

    public SchemaAwareChecker(TcpChecker tcpChecker, HttpChecker httpChecker) {
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
