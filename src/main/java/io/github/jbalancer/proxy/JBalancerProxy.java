package io.github.jbalancer.proxy;

import io.github.jbalancer.JBalancer;
import io.github.jbalancer.JBalancerCfg;
import io.github.jbalancer.metrics.ThreadPoolCollector;
import org.asynchttpclient.AsyncHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class JBalancerProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(JBalancerProxy.class);

    private final JBalancer jBalancer;
    private final JBalancerProxyApp jBalancerProxyApp;
    private final AsyncHttpClient httpClient;
    private final ThreadPoolCollector threadPoolCollector;

    private final Server httpServer;

    public JBalancerProxy(JBalancerCfg jBalancerCfg,
                          JBalancer jBalancer, JBalancerProxyApp jBalancerProxyApp,
                          AsyncHttpClient asyncHttpClient,
                          ThreadPoolCollector threadPoolCollector) {
        this.jBalancer = jBalancer;
        this.jBalancerProxyApp = jBalancerProxyApp;
        this.httpClient = asyncHttpClient;
        this.threadPoolCollector = threadPoolCollector;

        this.httpServer = createHttpServer(jBalancerCfg);
    }

    private Server createHttpServer(JBalancerCfg cfg) {

        final QueuedThreadPool threadPool = new QueuedThreadPool(250, 16);
        final Server server = new Server(threadPool);

        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(cfg.getPort());
        server.setConnectors(new Connector[]{connector});

        final FilterHolder sparkFilterHolder = new FilterHolder(jBalancerProxyApp);

        final ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/");
        servletHandler.addFilter(sparkFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(new HandlerList(createBalancingHandler(cfg), servletHandler));
        return server;
    }

    private BalancingHandler createBalancingHandler(JBalancerCfg cfg) {
        return new BalancingHandler(httpClient, jBalancer, threadPoolCollector);
    }

    @PostConstruct
    public synchronized void start() {

        try {
            if (httpServer.isRunning()) {
                LOGGER.debug("HTTP balancer server already running");
                return;
            }
            httpServer.start();
            LOGGER.info("HTTP balancer server started");
        } catch (Exception e) {
            throw new IllegalStateException("Error starting HTTP balancer server", e);
        }
    }

    @PreDestroy
    public synchronized void stop() {

        try {
            if (null != httpServer && httpServer.isRunning()) {
                httpServer.stop();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error stopping HTTP balancer server", e);
        }
    }
}
