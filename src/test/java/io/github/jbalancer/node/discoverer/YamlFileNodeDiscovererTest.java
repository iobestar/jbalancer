package io.github.jbalancer.node.discoverer;

import io.github.jbalancer.node.Node;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlFileNodeDiscovererTest {

    @Rule
    public TemporaryFolder discovererFolder = new TemporaryFolder();

    private YamlFileNodeDiscoverer yamlFileNodeDiscoverer;

    @Test
    public void discoverNodes() throws Exception {

        givenBalancerNodeFile("balancer1","nodes:\n" +
                "- connection: http://localhost:9090\n" +
                "  status: http://localhost:9090/status\n" +
                "  labels:\n" +
                "    id: 1\n" +
                "    location: altus\n" +
                "- connection: http://localhost:8080\n" +
                "  status: http://localhost:8080/status\n" +
                "  labels:\n" +
                "    id: 1\n" +
                "    location: altus\n");

        givenFileDiscoverer();

        assertThat(yamlFileNodeDiscoverer.discover("balancer1")).extracting(Node::getConnection).contains(URI.create("http://localhost:9090"), URI.create("http://localhost:8080"));
        assertThat(yamlFileNodeDiscoverer.discover("balancer1")).isNull();
    }

    private File givenBalancerNodeFile(String balancerId, String nodes) throws Exception {

        File nodesFile = discovererFolder.newFile(balancerId + ".yml");
        IOUtils.write(nodes, new FileOutputStream(nodesFile), Charset.defaultCharset());
        return nodesFile;
    }

    private void givenFileDiscoverer() {

        yamlFileNodeDiscoverer = new YamlFileNodeDiscoverer(discovererFolder.getRoot());
    }
}