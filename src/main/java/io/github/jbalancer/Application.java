package io.github.jbalancer;

import io.github.jbalancer.metrics.ThreadPoolCollector;
import io.github.jbalancer.node.discoverer.YamlNodeDiscoverer;
import io.github.jbalancer.proxy.YamlNodesStorage;
import io.github.jbalancer.strategy.RoundRobinStrategy;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

@Configuration
@ComponentScan()
public class Application {

    @Bean
    public JBalancerCfg jBalancerServiceCfg() {
        return () -> 8088;
    }

    @Bean
    public JBalancer jBalancer(YamlNodesStorage yamlNodesStorage) {

        final JBalancer jBalancer = new JBalancer.Builder().build();
        yamlNodesStorage.getBalancerIds().forEach(id -> jBalancer.create(id, new RoundRobinStrategy(), new YamlNodeDiscoverer(s -> yamlNodesStorage.getYamlNodes(id))));
        return jBalancer;
    }

    @Bean
    public AsyncHttpClient asyncHttpClient(JBalancerCfg jBalancerCfg) {

        final DefaultAsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().build();
        return new DefaultAsyncHttpClient(config);
    }

    @Bean
    public ThreadPoolCollector threadPoolCollector() {
        return ThreadPoolCollector.getInstance();
    }

    @Bean(destroyMethod = "close")
    public DB db() {
        return DBMaker.fileDB("balancer.db").make();
    }

    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Application.class);
        ((AbstractApplicationContext)applicationContext).registerShutdownHook();
    }
}
