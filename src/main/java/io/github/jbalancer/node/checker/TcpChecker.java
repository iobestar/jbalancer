package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;

public class TcpChecker implements Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpChecker.class);

    private final int connectionTimeout;

    TcpChecker() {
        this(2000);
    }

    public TcpChecker(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void check(Node node) {

        URI status = node.getStatus();
        if (null == status) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(status.getHost(), status.getPort()), connectionTimeout);
            node.setActive(true);
            node.setAlive(true);
            node.setCheckStatus(null);
        } catch (SocketTimeoutException e) {
            node.setActive(false);
            node.setAlive(true);
            node.setCheckStatus(e.getClass().getSimpleName() + ":" + e.getMessage());
            LOGGER.trace("Socket timeout checking node {}", status.toString(), e);
        } catch (Exception e) {
            node.setActive(false);
            node.setAlive(false);
            node.setCheckStatus(e.getClass().getSimpleName() + ":" + e.getMessage());
            LOGGER.trace("Error checking node {}", status.toString(), e);
        }
    }
}
