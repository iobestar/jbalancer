package io.github.jbalancer.node.discoverer;

import org.apache.commons.io.FileUtils;
import io.github.jbalancer.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeFileDiscoverer implements Discoverer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFileDiscoverer.class);

    private final File nodeDirectory;
    private final Constructor nodesConstructor;

    private volatile long lastModified = 0L;

    public NodeFileDiscoverer(File nodeDirectory) {
        this.nodeDirectory = setupNodeDirectory(nodeDirectory);
        this.nodesConstructor = createConstructor();
    }

    private File setupNodeDirectory(File directory) {

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + directory.getAbsolutePath());
            }
            return directory;
        }

        if (!directory.isDirectory()) {
            throw new IllegalStateException("File not directory: " + directory);
        }

        return directory;
    }

    private Constructor createConstructor() {

        Constructor constructor = new Constructor(FileNodes.class);
        TypeDescription carDescription = new TypeDescription(FileNode.class);
        carDescription.putListPropertyType("nodes", FileNode.class);
        constructor.addTypeDescription(carDescription);
        return constructor;
    }

    @Override
    public List<Node> discover(String balancerId) {

        File nodeFile = getNodeFile(balancerId);

        if (!nodeFile.exists()) {
            LOGGER.warn("Node file not exists: {}", nodeFile.getAbsolutePath());
            return null;
        }

        if (nodeFile.isDirectory()) {
            LOGGER.warn("Node file is directory: {}", nodeFile.getAbsolutePath());
            return null;
        }

        if (lastModified == nodeFile.lastModified()) {
            LOGGER.debug("Nodes file unchanged");
            return null;
        }

        try {
            FileNodes fileNodes = (FileNodes) createYaml().load(FileUtils.openInputStream(nodeFile));
            List<FileNode> nodes = fileNodes.getNodes();

            lastModified = nodeFile.lastModified();
            return nodes.stream()
                    .map(fe -> new Node(fe.getLabels(), URI.create(fe.getConnection()), URI.create(fe.getStatus())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error reading nodes from file: {}", nodeFile.getAbsolutePath(), e);
            return null;
        }
    }

    private File getNodeFile(String balancerId) {
        return new File(nodeDirectory, balancerId + ".yml");
    }

    private Yaml createYaml() {
        return new Yaml(nodesConstructor);
    }

    public static class FileNode {

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

    public static class FileNodes {

        private List<FileNode> nodes;

        public List<FileNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<FileNode> nodes) {
            this.nodes = nodes;
        }
    }
}
