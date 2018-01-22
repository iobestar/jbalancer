package io.github.jbalancer.proxy;

import io.github.jbalancer.Balancer;
import io.github.jbalancer.JBalancer;
import io.github.jbalancer.metrics.ThreadPoolCollector;
import io.github.jbalancer.node.BalancedNode;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class BalancingHandler extends AbstractHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancingHandler.class);

    private static final String BALANCER_ID_HEADER = "X-Balancer-Id";

    private final AsyncHttpClient asyncHttpClient;
    private final JBalancer jBalancer;
    private final ExecutorService proxyExecutor = Executors.newCachedThreadPool();

    public BalancingHandler(AsyncHttpClient asyncHttpClient,
                            JBalancer jBalancer,
                            ThreadPoolCollector threadPoolCollector) {
        this.asyncHttpClient = asyncHttpClient;
        this.jBalancer = jBalancer;
        threadPoolCollector.register("balancing_executor", (ThreadPoolExecutor) proxyExecutor);
    }

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        RequestBuilder requestBuilder;
        try {

            final String mapping = getMapping(request);
            if (null == mapping) return;

            baseRequest.setHandled(true);

            requestBuilder = new RequestBuilder()
                    .setMethod(request.getMethod())
                    .setUrl(mapping)
                    .setHeaders(extractHeaders(request, true))
                    .setBody(request.getInputStream());
        } catch (Exception e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error("Balancer error: {}", e.getMessage());
            }
            response.sendError(500, e.getMessage());
            return;
        }
        AsyncContext asyncContext = request.startAsync();
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {

                LOGGER.debug("async completed");
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {

                LOGGER.debug("async timeout");
                HttpServletResponse httpServletResponse = (HttpServletResponse) asyncEvent.getSuppliedResponse();
                httpServletResponse.setStatus(408);
                asyncEvent.getAsyncContext().complete();
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {

                LOGGER.error("async error", asyncEvent.getThrowable());
                asyncEvent.getAsyncContext().complete();
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

                LOGGER.debug("async started");
            }
        });

        asyncHttpClient.prepareRequest(requestBuilder).execute(new AsyncCompletionHandler<Object>() {

            @Override
            public void onThrowable(Throwable t) {

                CompletableFuture.runAsync(() -> {
                    try {
                        response.sendError(500, t.getMessage());
                    } catch (Exception e) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error("Balancer error: {}", e.getMessage());
                        }
                    } finally {
                        asyncContext.complete();
                    }
                }, proxyExecutor);
            }

            @Override
            public Object onCompleted(Response proxyResponse) throws Exception {

                CompletableFuture.runAsync(() -> {
                    try {
                        response.setStatus(proxyResponse.getStatusCode());
                        copyResponseHeaders(proxyResponse, response);
                        IOUtils.copy(proxyResponse.getResponseBodyAsStream(), response.getOutputStream());
                        response.getOutputStream().flush();
                    } catch (Exception e) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error("Balancer error: {}", e.getMessage());
                        }
                    } finally {
                        asyncContext.complete();
                    }
                }, proxyExecutor);
                return null;
            }

            private void copyResponseHeaders(Response response, HttpServletResponse servletResponse) {

                HttpHeaders headers = response.getHeaders();
                headers.entries().forEach(header -> servletResponse.setHeader(header.getKey(), header.getValue()));
            }
        });
    }

    private String getMapping(HttpServletRequest request) {

        final String balancerId = request.getHeader(BALANCER_ID_HEADER);

        if (null == balancerId) {
            return null;
        }

        final Balancer balancer = jBalancer.getBalancer(balancerId);
        if (null == balancer) {
            throw new IllegalStateException("Missing balancer with id " + balancerId);
        }

        BalancedNode node = balancer.getBalanced();
        if (null == node) {
            throw new IllegalStateException("No available nodes for balancer " + balancerId);
        }

        final String target = node.getConnection().toString();
        if (null == target) return null;

        String targetPath = StringUtils.removeStart(request.getRequestURI(), request.getContextPath() + request.getServletPath());
        return target + targetPath + Optional.ofNullable(request.getQueryString()).map(qs -> "?" + qs).orElse(StringUtils.EMPTY);
    }

    private Map<String, Collection<String>> extractHeaders(HttpServletRequest request, boolean excludeContentLength) {

        Map<String, Collection<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase("content-length") && excludeContentLength) {
                continue;
            }
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headers;
    }

    @Override
    public void destroy() {

        if (proxyExecutor.isTerminated() || proxyExecutor.isShutdown()) {
            return;
        }

        List<Runnable> notCompletedCallbacks = proxyExecutor.shutdownNow();
        if (!notCompletedCallbacks.isEmpty()) {
            LOGGER.warn("Balancer tasks waiting for execution: {}", StringUtils.join(notCompletedCallbacks, ",\n"));
        }

        try {
            proxyExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            LOGGER.info("Balancer executor stopped - actively executing tasks terminated");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Wait for baancer executor shutdown interrupted", e);
        }
        super.destroy();
    }
}
