package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SchemaAwareCheckerTest {

    @Mock
    private HttpChecker httpChecker;

    @Mock
    private TcpChecker tcpChecker;

    @Mock
    private Node node;

    @InjectMocks
    private SchemaAwareChecker schemaAwareChecker;

    @Test
    public void invokeHttpCheckerWhenSchemaHttp() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("http://localhost:9090"));

        schemaAwareChecker.check(node);

        verify(httpChecker, times(1)).check(node);
    }

    @Test
    public void invokeHttpCheckerWhenSchemaHttpAsUpperCase() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("HTTP://localhost:9090"));

        schemaAwareChecker.check(node);

        verify(httpChecker, times(1)).check(node);
    }

    @Test
    public void invokeHttpCheckerWhenSchemaTcp() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("tcp://localhost:9090"));

        schemaAwareChecker.check(node);

        verify(tcpChecker, times(1)).check(node);
    }

    @Test
    public void invokeHttpCheckerWhenSchemaTcpAsUpperCase() throws Exception {

        when(node.getStatus()).thenReturn(URI.create("TCP://localhost:9090"));

        schemaAwareChecker.check(node);

        verify(tcpChecker, times(1)).check(node);
    }

    @Test
    public void fallbackToConnectionWhenStatusIsNull() throws Exception {

        when(node.getStatus()).thenReturn(null);
        when(node.getConnection()).thenReturn(URI.create("tcp://localhost:9090"));

        schemaAwareChecker.check(node);

        verify(tcpChecker, times(1)).check(node);
    }

    @Test
    public void noInteractionsWhenStatusAndConnectionIsNull() throws Exception {

        schemaAwareChecker.check(node);

        verifyNoMoreInteractions(httpChecker, tcpChecker);
    }
}