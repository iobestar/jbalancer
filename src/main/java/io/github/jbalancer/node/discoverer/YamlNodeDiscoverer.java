package io.github.jbalancer.node.discoverer;

import io.github.jbalancer.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Discovers nodes from YAML source function which maps balancer id to YAML node definition.
 *
 * Nodes YAML format:
 * <code>
 * nodes:
 * - connection: tcp://host1:3000
 *   status: tcp://127.0.0.1:3001
 *   labels:
 *     label-key: label-value
 * - connection: tcp://host2:3000
 *   status: tcp://127.0.0.1:3001
 *   labels:
 *     label-key: label-value
 * </code>
 */
public class YamlNodeDiscoverer implements Discoverer {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlNodeDiscoverer.class);

    private final Function<String, String> yamlSource;
    private final Constructor nodesConstructor;

    public YamlNodeDiscoverer(Function<String, String> yamlSource) {
        this.yamlSource = yamlSource;
        this.nodesConstructor = createConstructor();
    }

    private Constructor createConstructor() {

        Constructor constructor = new Constructor(YamlNodes.class);
        TypeDescription carDescription = new TypeDescription(YamlNode.class);
        carDescription.putListPropertyType("nodes", YamlNode.class);
        constructor.addTypeDescription(carDescription);
        return constructor;
    }

    @Override
    public List<Node> discover(String balancerId) {

        try {
            final String yamlNodes = yamlSource.apply(balancerId);
            if (null == yamlNodes) return null;

            YamlNodes fileNodes = (YamlNodes) createYaml().load(yamlSource.apply(balancerId));
            List<YamlNode> nodes = fileNodes.getNodes();
            return nodes.stream()
                    .map(fe -> new Node(fe.getLabels(), URI.create(fe.getConnection()), URI.create(fe.getStatus())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error loading nodes from YAML", e);
            return null;
        }
    }

    private Yaml createYaml() {
        return new Yaml(nodesConstructor);
    }

    public static class YamlNode {

        private String connection;
        private String status;
        private Map<String, String> labels;

        public String getConnection() {
            return connection;
        }

        public void setConnection(String connection) {
            this.connection = connection;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
    }

    public static class YamlNodes {

        private List<YamlNode> nodes;

        public List<YamlNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<YamlNode> nodes) {
            this.nodes = nodes;
        }
    }
}
