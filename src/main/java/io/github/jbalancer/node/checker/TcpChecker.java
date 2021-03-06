package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Optional;

/**
 *  Checks and updates {@link Node} state based on TCP connection aliveness.
 *
 *  If TCP status URI is reachable node is active and alive. Otherwise node is not active and not alive.
 */
public class TcpChecker implements Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpChecker.class);

    private final int connectionTimeout;

    TcpChecker() {
        this(2000);
    }

    TcpChecker(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void check(Node node) {

        if (null == node) {
            LOGGER.debug("Node is null");
            return;
        }

        final URI status = node.getStatus();
        if (null == status) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(status.getHost(), status.getPort()), connectionTimeout);
            node.setActive(true);
            node.setAlive(true);
            node.setCheckStatus(null);
        } catch (ConnectException e) {
            final String message = Optional.ofNullable(e.getMessage()).orElse("").toLowerCase();
            if (message.contains("refused")) {
                node.setActive(false);
                node.setAlive(false);
            }
            node.setCheckStatus(e.getClass().getSimpleName() + ":" + e.getMessage());
        } catch (Exception e) {
            node.setActive(false);
            node.setAlive(false);
            node.setCheckStatus(e.getClass().getSimpleName() + ":" + e.getMessage());
            LOGGER.error("Error checking node: {}", node, e);
        }
    }
}
