package io.github.jbalancer.node.checker;

public class CheckerFactory {

    /**
     * Creates new instance of {@link SchemaAwareChecker}.
     *
     * @see Checker
     * @param httpClient instance of {@link HttpClient}
     * @return instance of {@link Checker}
     */
    public static Checker createDefault(HttpClient httpClient) {
        return new SchemaAwareChecker(new TcpChecker(httpClient.getConnectionTimeout()), new HttpChecker(httpClient));
    }
}
