package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketTimeoutException;
import java.net.URI;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HttpCheckerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpClient httpClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CloseableHttpResponse response;

    @Mock
    private Node node;

    @InjectMocks
    private HttpChecker httpChecker;

    @Test
    public void noInteractionsWhenStatusNodeIsNull() throws Exception {

        when(node.getStatus()).thenReturn(null);

        httpChecker.check(node);

        verifyNoMoreInteractions(httpClient);
        verify(node, never()).setActive(anyBoolean());
        verify(node, never()).setAlive(anyBoolean());
    }

    @Test
    public void nodeAliveAndActiveWhenHead200() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("http://localhost:9090"));
        when(response.getStatusLine().getStatusCode()).thenReturn(200);
        when(httpClient.httpClient().execute(any(HttpHead.class))).thenReturn(response);

        httpChecker.check(node);

        verify(node, times(1)).setActive(true);
        verify(node, times(1)).setAlive(true);
    }

    @Test
    public void nodeAliveAndNotActiveWhenHeadNot200() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("http://localhost:9090"));
        when(response.getStatusLine().getStatusCode()).thenReturn(500);
        when(httpClient.httpClient().execute(any(HttpHead.class))).thenReturn(response);

        httpChecker.check(node);

        verify(node, times(1)).setActive(false);
        verify(node, times(1)).setAlive(true);
    }

    @Test
    public void noChangeWhenSocketTimeoutException() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("http://localhost:9090"));
        when(httpClient.httpClient().execute(any(HttpHead.class))).thenThrow(new SocketTimeoutException("socket timeout"));

        httpChecker.check(node);

        verify(node, times(0)).setActive(anyBoolean());
        verify(node, times(0)).setAlive(anyBoolean());
        verify(node, times(1)).setCheckStatus(anyString());
    }

    @Test
    public void noChangeWhenException() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("http://localhost:9090"));
        when(httpClient.httpClient().execute(any(HttpHead.class))).thenThrow(new RuntimeException("socket timeout"));

        httpChecker.check(node);

        verify(node, times(0)).setActive(anyBoolean());
        verify(node, times(0)).setAlive(anyBoolean());
        verify(node, times(1)).setCheckStatus(anyString());
    }
}