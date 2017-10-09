package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URI;

/**
 *  Checks and updates {@link Node} state based on HTTP response and status.
 *
 *  If HTTP status URI returns code 200 node is active and alive. Otherwise node is not active.
 *  If HTTP status URI is not reachable then node is not active and not alive.
 */
public class HttpChecker implements Checker {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpChecker.class);

    private final HttpClient httpClient;

    HttpChecker(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void check(Node node) {

        if (null == node) {
            LOGGER.debug("Node is null");
            return;
        }

        URI status = node.getStatus();
        if (null == status) {
            LOGGER.warn("Node status URI is null: {}", node.toString());
            return;
        }

        try (CloseableHttpResponse response = httpClient.httpClient().execute(new HttpHead(status))) {
            node.setActive(response.getStatusLine().getStatusCode() == 200);
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
