package io.github.jbalancer.node.checker;

public class CheckerFactory {

    public static Checker createDefault(HttpClient httpClient) {
        return new SchemaAwareChecker(new TcpChecker(httpClient.getConnectionTimeout()), new HttpChecker(httpClient));
    }
}
