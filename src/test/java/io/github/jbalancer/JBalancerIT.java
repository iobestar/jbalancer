package io.github.jbalancer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import io.github.jbalancer.node.BalancedNode;
import io.github.jbalancer.node.discoverer.YamlFileNodeDiscoverer;
import io.github.jbalancer.strategy.RoundRobinStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class JBalancerIT {

    @Rule
    public TemporaryFolder discovererFolder = new TemporaryFolder();

    private static final WireMockServer SERVER_1 = new WireMockServer(wireMockConfig().dynamicPort());
    private static int serverPort1;

    private static final WireMockServer SERVER_2 = new WireMockServer(wireMockConfig().dynamicPort());
    private static int serverPort2;

    private JBalancer jBalancer = new JBalancer.Builder().checkPeriod(1000).discoverPeriod(1000).build();

    private Balancer balancer;

    @BeforeClass
    public static void setUpClass() throws Exception {

        SERVER_1.start();
        serverPort1 = SERVER_1.port();
        SERVER_2.start();
        serverPort2 = SERVER_2.port();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        SERVER_1.stop();
        SERVER_2.stop();
    }

    @After
    public void tearDown() throws Exception {

        jBalancer.destroy();

        SERVER_1.resetAll();
        SERVER_2.resetAll();
    }

    @Test
    public void balanceOverAvailableDiscoveredNodes() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        for (int i = 0; i < 10; i++) {
            assertThat(balancer.getBalanced()).isNotNull();
        }
    }

    @Test
    public void oneNodeServerIsDisabledDuringBalancing() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        assertRemovedFromBalancer(
                () -> SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(404))),
                "http://localhost:" + serverPort1);
    }

    @Test
    public void oneNodeServerIsStoppedDuringBalancing() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        assertRemovedFromBalancer(()-> {
            SERVER_2.stop();
            Awaitility.await().atMost(Duration.FIVE_SECONDS).until(() -> !SERVER_2.isRunning());
        }, "http://localhost:" + serverPort1);
    }

    @Test
    public void oneNodeIsDisabledDuringBalancing() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        assertRemovedFromBalancer(() -> jBalancer.disableNodes(balancer.getId(),
                bn -> bn.getConnection().toString().equalsIgnoreCase("http://localhost:" + serverPort2)),
                "http://localhost:" + serverPort1);
    }

    @Test
    public void fetchBalancerById() throws Exception {

        givenBalancer();

        assertThat(jBalancer.getBalancer(balancer.getId())).isSameAs(balancer);
    }

    @Test
    public void fetchAllBalancers() throws Exception {

        givenBalancer();

        assertThat(jBalancer.getBalancers()).containsOnly(balancer);
    }

    @Test
    public void disableAllNodes() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        jBalancer.disableNodes(balancer.getId(), bn -> true);

        assertThat(balancer.getBalanced()).isNull();
    }

    @Test
    public void enableAllNodes() throws Exception {

        SERVER_1.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));
        SERVER_2.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(200)));

        givenBalancer();

        jBalancer.disableNodes(balancer.getId(), bn -> true);
        assertThat(balancer.getBalanced()).isNull();

        jBalancer.enableNodes(balancer.getId(), bn -> true);
        assertThat(balancer.getBalanced()).isNotNull();
    }

    private void assertRemovedFromBalancer(Runnable disable, String expectedUrl) throws Exception {

        Set<String> balancedUrls = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            balancedUrls.clear();
            for (int j = 0; j < 10; j++) {
                BalancedNode node = balancer.getBalanced();
                balancedUrls.add(node.getConnection().toString());
            }
            Thread.sleep(1000);
            disable.run();
        }

        assertThat(balancedUrls).containsOnly(expectedUrl);
    }

    private void givenBalancer() throws Exception {

        balancer = jBalancer.create("anybalancer", new RoundRobinStrategy(),
                new YamlFileNodeDiscoverer(givenNodesFile("anybalancer").getParentFile()));
    }

    private File givenNodesFile(String name) throws Exception {

        File nodesFile = discovererFolder.newFile(name + ".yml");
        IOUtils.write(getNodesFileContent(), new FileOutputStream(nodesFile), Charset.defaultCharset());
        return nodesFile;
    }

    private static final String NODES_TEMPLATE =
            "nodes:\n" +
                    "- connection: http://localhost:${port1}\n" +
                    "  status: http://localhost:${port1}/status\n" +
                    "  labels:\n" +
                    "    id: 1\n" +
                    "    location: A\n" +
                    "- connection: http://localhost:${port2}\n" +
                    "  status: http://localhost:${port2}/status\n" +
                    "  labels:\n" +
                    "    id: 2\n" +
                    "    location: A\n";

    private static String getNodesFileContent() {
        return NODES_TEMPLATE
                .replace("${port1}", "" + serverPort1)
                .replace("${port2}", "" + serverPort2);
    }
}