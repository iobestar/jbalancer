package io.github.jbalancer.node.discoverer;

import io.github.jbalancer.node.Node;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Discovers nodes from YAML files. For each balancer exists YAML file in {@code nodeDirectory}.
 * Files are named with balancer ids.
 *
 * @see YamlNodeDiscoverer
 */
public class YamlFileNodeDiscoverer implements Discoverer {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlFileNodeDiscoverer.class);

    private final File nodeDirectory;

    private volatile long lastModified = 0L;

    public YamlFileNodeDiscoverer(File nodeDirectory) {
        this.nodeDirectory = setupNodeDirectory(nodeDirectory);

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
            lastModified = nodeFile.lastModified();
            return new YamlNodeDiscoverer(s -> {
                try {
                    return FileUtils.readFileToString(nodeFile, Charset.defaultCharset());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).discover(balancerId);
        } catch (Exception e) {
            LOGGER.error("Error reading nodes from file: {}", nodeFile.getAbsolutePath(), e);
            return null;
        }
    }

    private File getNodeFile(String balancerId) {
        return new File(nodeDirectory, balancerId + ".yml");
    }
}
