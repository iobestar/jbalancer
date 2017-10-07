package io.github.jbalancer.node.checker;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().dynamicHttpsPort());

    private HttpClient httpClient;

    @Test
    public void performHeadRequestViaHttp() throws Exception {

        givenHttpClient();
        wireMockRule.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(201)));

        CloseableHttpResponse response = httpClient.httpClient().execute(new HttpHead("http://localhost:" + wireMockRule.port() + "/status"));

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
    }

    @Test
    public void performHeadRequestViaHttps() throws Exception {

        givenHttpClient();
        wireMockRule.stubFor(head(urlEqualTo("/status")).willReturn(aResponse().withStatus(201)));

        CloseableHttpResponse response = httpClient.httpClient().execute(new HttpHead("https://localhost:" + wireMockRule.httpsPort() + "/status"));

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
    }

    private void givenHttpClient() {

        httpClient = HttpClient.newInstance();
    }
}