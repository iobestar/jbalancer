package io.github.jbalancer;

import io.github.jbalancer.node.BalancedNode;
import io.github.jbalancer.node.Node;
import io.github.jbalancer.node.checker.Checker;
import io.github.jbalancer.node.checker.CheckerFactory;
import io.github.jbalancer.node.checker.HttpClient;
import io.github.jbalancer.node.discoverer.Discoverer;
import io.github.jbalancer.strategy.Strategy;
import io.github.jbalancer.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class JBalancer implements BalancerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JBalancer.class);

    private final HttpClient httpClient = HttpClient.newInstance();
    private final Checker checker = CheckerFactory.createDefault(httpClient);

    private final Map<String, DefaultBalancer> balancers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService checkService = Executors.newScheduledThreadPool(1,
            NamedThreadFactory.create("jbalancer-check"));
    private final ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1,
            NamedThreadFactory.create("jbalancer-discover"));

    private final Object createLock = new Object();

    private JBalancer(long checkPeriod, long discoverPeriod) {

        startCheck(checkPeriod);
        startDiscover(discoverPeriod);
    }

    private void startCheck(long checkPeriod) {

        if (checkPeriod <= 0) return;

        checkService.scheduleWithFixedDelay(() -> {
            LOGGER.debug("Performing node check");
            try {
                balancers.values().forEach(DefaultBalancer::check);
            } catch (Exception e) {
                LOGGER.error("Error performing node check", e);
            }
        }, 0L, checkPeriod, TimeUnit.MILLISECONDS);

        LOGGER.info("JBalancer node checker started");
    }

    private void startDiscover(long discoverPeriod) {

        if (discoverPeriod <= 0) return;

        discoverService.scheduleWithFixedDelay(() -> {
            LOGGER.debug("Performing node discovery");
            try {
                balancers.values().forEach(DefaultBalancer::discover);
            } catch (Exception e) {
                LOGGER.error("Error performing node discovery", e);
            }
        }, 0L, discoverPeriod, TimeUnit.MILLISECONDS);

        LOGGER.info("JBalancer node discoverer started");
    }

    /**
     * Returns {@link Balancer} instance by unique identifier.
     *
     * @param balancerId balancer unique identifier
     * @return balancer or null if not exists
     */
    public Balancer getBalancer(String balancerId) {
        return balancers.get(balancerId);
    }

    /**
     * Returns list of all known {@link Balancer}.
     *
     * @return list of {@link Balancer}
     */
    public List<Balancer> getBalancers() {
        return Collections.unmodifiableList(new ArrayList<>(balancers.values()));
    }

    /**
     * Enables all endpoints selected with balancer id and selector predicate.
     *
     * @param balancerId balancer unique identifier
     * @param selector node slector predicate
     */
    public void enableNodes(String balancerId, Predicate<BalancedNode> selector) {

        Balancer balancer = getBalancer(balancerId);
        if (null == balancer) return;

        balancer.getAll().stream().filter(selector).forEach(bn -> ((Node) bn).enable());
    }

    /**
     * Disables all endpoints selected with balancer id and selector predicate.
     *
     * @param balancerId balancer unique identifier
     * @param selector node slector predicate
     */
    public void disableNodes(String balancerId, Predicate<BalancedNode> selector) {

        Balancer balancer = getBalancer(balancerId);
        if (null == balancer) return;

        balancer.getAll().stream().filter(selector).forEach(bn -> ((Node) bn).disable());
    }

    @Override
    public Balancer create(String id, Strategy strategy, Discoverer discoverer) {
        return createInternal(id, strategy, discoverer, Collections.emptyList());
    }

    @Override
    public Balancer create(String id, Strategy strategy, List<Node> nodes) {
        return createInternal(id, strategy, null, nodes);
    }

    private Balancer createInternal(String id, Strategy strategy, Discoverer discoverer, List<Node> nodes) {

        synchronized (createLock) {
            if (null != id && balancers.containsKey(id)) {
                throw new IllegalStateException("Balancer with id " + id + " already exists");
            }

            DefaultBalancer balancer = new DefaultBalancer(id, strategy, discoverer, checker, nodes);
            return balancers.computeIfAbsent(balancer.getId(), s -> balancer);
        }
    }

    /**
     * Destroys and releases all used resources.
     */
    public void destroy() {

        shutdownService(checkService);
        shutdownService(discoverService);
        httpClient.close();

        LOGGER.info("JBalancer successfully destroyed");
    }

    private void shutdownService(ExecutorService executorService) {

        if (executorService.isShutdown()) return;
        if (executorService.isTerminated()) return;

        executorService.shutdownNow();

        try {
            executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            LOGGER.warn("Executor service termination interrupted", e);
        }
    }

    public static class Builder {

        private long checkPeriod = 2000;
        private long discoverPeriod = 30000;

        public Builder checkPeriod(long value) {

            checkPeriod = value;
            return this;
        }

        public Builder discoverPeriod(long value) {

            discoverPeriod = value;
            return this;
        }

        public JBalancer build() {
            return new JBalancer(checkPeriod, discoverPeriod);
        }
    }
}
