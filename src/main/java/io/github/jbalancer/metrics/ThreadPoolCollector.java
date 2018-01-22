package io.github.jbalancer.metrics;

import io.prometheus.client.Collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * Collects metrics of registered {@link ThreadPoolExecutor}:
 * <ul>
 * <li>CORE_POOL_SIZE</li>
 * <li>LARGEST_POOL_SIZE</li>
 * <li>MAXIMUM__POOL_SIZE</li>
 * <li>CURRENT__POOL_SIZE</li>
 * <li>ACTIVE_THREADS</li>
 * <li>COMPLETED_TASKS</li>
 * <li>QUEUED_TASKS</li>
 * </ul>
 */
public class ThreadPoolCollector extends Collector {

    private enum MetricFamily {
        CORE_POOL_SIZE("thread_pool_core_pool_size", Type.GAUGE, "Thread pool core pool size", executor -> (long) executor.getCorePoolSize()),
        LARGEST_POOL_SIZE("thread_pool_largest_pool_size", Type.GAUGE, "Thread pool largest pool size", executor -> (long) executor.getLargestPoolSize()),
        MAXIMUM__POOL_SIZE("thread_pool_maximum_pool_size", Type.GAUGE, "Thread pool maximum pool size", executor -> (long) executor.getMaximumPoolSize()),
        CURRENT__POOL_SIZE("thread_pool_current_pool_size", Type.GAUGE, "Thread pool current pool size", executor -> (long) executor.getPoolSize()),
        ACTIVE_THREADS("thread_pool_active_threads", Type.GAUGE, "Number of currently active threads", executor -> (long) executor.getActiveCount()),
        COMPLETED_TASKS("thread_pool_completed_tasks", Type.GAUGE, "Number of completed tasks", ThreadPoolExecutor::getCompletedTaskCount),
        QUEUED_TASKS("thread_pool_queued_tasks", Type.GAUGE, "Number of tasks in queue", executor -> (long) executor.getQueue().size());

        private final String name;
        private final Type type;
        private final String help;
        private final Function<ThreadPoolExecutor, Long> sampleFunction;

        MetricFamily(String name, Type type, String help, Function<ThreadPoolExecutor, Long> sampleFunction) {
            this.name = name;
            this.type = type;
            this.help = help;
            this.sampleFunction = sampleFunction;
        }

        private MetricFamilySamples createSamples(List<MetricFamilySamples.Sample> samples) {
            return new MetricFamilySamples(name, type, help, samples);
        }
    }

    private static class LazyHolder {
        static final ThreadPoolCollector INSTANCE = new ThreadPoolCollector();
    }

    public static ThreadPoolCollector getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static ThreadPoolCollector newInstance() {
        return new ThreadPoolCollector();
    }

    private final Map<String, ThreadPoolExecutor> threadPools = new ConcurrentHashMap<>();

    @Override
    public List<MetricFamilySamples> collect() {

        List<MetricFamilySamples> familySamples = new ArrayList<>();
        for (MetricFamily metricFamily : MetricFamily.values()) {
            List<MetricFamilySamples.Sample> samples = new ArrayList<>();
            threadPools.forEach((k, v) -> samples.add(new MetricFamilySamples.Sample(metricFamily.name, Collections.singletonList("name"),
                    Collections.singletonList(k), getSampleValue(metricFamily, v))));
            familySamples.add(metricFamily.createSamples(samples));
        }
        return familySamples;
    }

    private Long getSampleValue(MetricFamily metricFamily, ThreadPoolExecutor threadPool) {
        return metricFamily.sampleFunction.apply(threadPool);
    }

    /**
     * Registers {@link ThreadPoolExecutor} to collector under name {@code threadPoolName}.
     *
     * @param threadPoolName name of thread pool
     * @param threadPool     instance of thread pool
     */
    public void register(String threadPoolName, ThreadPoolExecutor threadPool) {

        Objects.requireNonNull(threadPoolName, "threadPoolName must be non null");
        Objects.requireNonNull(threadPool, "threadPool must be non null");

        synchronized (threadPools) {
            if (threadPools.containsKey(threadPoolName)) {
                throw new IllegalStateException("Thread pool already exists: " + threadPoolName);
            }
            threadPools.put(threadPoolName, threadPool);
        }
    }

    /**
     * Removes all registered thread pools.
     */
    public void clear() {

        synchronized (threadPools) {
            threadPools.clear();
        }
    }
}