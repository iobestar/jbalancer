package io.github.jbalancer.node.checker;


import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private final CloseableHttpClient httpClient;
    private final int connectionTimeout;
    private final int executionTimeout;

    private HttpClient(int connectionTimeout, int executionTimeout,
                       URL proxyUrl, String proxyUsername,
                       String proxyPassword) throws Exception {

        this.connectionTimeout = connectionTimeout;
        this.executionTimeout = executionTimeout;
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(createRequestConfig(connectionTimeout, executionTimeout))
                .setConnectionManager(createConnectionManager());
        if (proxyUrl != null) {
            httpClientBuilder.setProxy(createProxy(proxyUrl));
            if (proxyUsername != null && proxyPassword != null) {
                httpClientBuilder.setDefaultCredentialsProvider(createProxyCredentialsProvider(proxyUrl, proxyUsername, proxyPassword));
            }
        }
        this.httpClient = httpClientBuilder.build();
    }

    public static HttpClient newInstance() {
        return new HttpClient.Builder().build();
    }

    private RequestConfig createRequestConfig(int connectionTimeout, int executionTimeout) {
        return RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(executionTimeout)
                .build();
    }

    private SSLContext createSSLContext() throws Exception {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new TrustEveryone()}, new SecureRandom());
        return sslContext;
    }

    private HttpClientConnectionManager createConnectionManager() throws Exception {

        PlainConnectionSocketFactory plainsf = new PlainConnectionSocketFactory();
        LayeredConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createSSLContext(), (s, sslSession) -> true);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslsf)
                .build();
        return new PoolingHttpClientConnectionManager(registry);
    }

    private HttpHost createProxy(URL proxyUrl) {
        return new HttpHost(
                proxyUrl.getHost(),
                proxyUrl.getPort(),
                proxyUrl.getProtocol()
        );
    }

    private CredentialsProvider createProxyCredentialsProvider(URL proxyUrl, String proxyUsername, String proxyPassword) {

        AuthScope proxyScope = new AuthScope(proxyUrl.getHost(), proxyUrl.getPort());
        Credentials proxyCreds = new UsernamePasswordCredentials(proxyUsername, proxyPassword);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(proxyScope, proxyCreds);
        return credentialsProvider;
    }

    public CloseableHttpClient httpClient() {
        return httpClient;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    @Override
    public void close() {

        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static class Builder {

        private int connectionTimeout = 2000;
        private int executionTimeout = 2000;
        private URL proxyUrl;
        private String proxyUsername;
        private String proxyPassword;

        public Builder setConnectionTimeout(int value) {
            connectionTimeout = value;
            return this;
        }

        public Builder setExecutionTimeout(int value) {
            executionTimeout = value;
            return this;
        }

        public Builder setProxyUrl(URL value) {
            proxyUrl = value;
            return this;
        }

        public Builder setProxyUsername(String value) {
            proxyUsername = value;
            return this;
        }

        public Builder setProxyPassword(String value) {
            proxyPassword = value;
            return this;
        }

        public HttpClient build() {

            try {
                return new HttpClient(connectionTimeout, executionTimeout,
                        proxyUrl, proxyUsername, proxyPassword);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class TrustEveryone implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
