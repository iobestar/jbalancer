package io.github.jbalancer.node.checker;

import io.github.jbalancer.node.Node;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TcpCheckerTest {

    private ServerSocket serverSocket;

    @Mock
    private Node node;

    private TcpChecker tcpChecker;

    @After
    public void tearDown() throws Exception {

        if (null != serverSocket) {
            serverSocket.close();
        }
    }

    @Test
    public void noInteractionsWhenStatusNodeIsNull() throws Exception {

        when(node.getStatus()).thenReturn(null);
        givenTcpChecker();

        tcpChecker.check(node);

        verify(node, never()).setActive(anyBoolean());
        verify(node, never()).setAlive(anyBoolean());
    }

    @Test
    public void nodeAliveAndActiveWhenTcpOk() throws Exception {

        int port = givenServerSocket();
        when(node.getStatus()).thenReturn(URI.create("tcp://localhost:" + port));
        givenTcpChecker();

        tcpChecker.check(node);

        verify(node, times(1)).setActive(true);
        verify(node, times(1)).setAlive(true);
    }

    @Test
    public void nodeAliveAndNotActiveWhenException() throws Exception {

        givenTcpChecker();
        when(node.getStatus()).thenReturn(URI.create("tcp://localhost:44000"));

        tcpChecker.check(node);

        verify(node, times(1)).setActive(false);
        verify(node, times(1)).setAlive(false);
        verify(node, times(1)).setCheckStatus(anyString());
    }

    private void givenTcpChecker(int connectionTimeout) {

        tcpChecker = new TcpChecker(connectionTimeout);
    }

    private void givenTcpChecker() {

        givenTcpChecker(1000);
    }

    private int givenServerSocket() throws Exception {

        serverSocket = new ServerSocket(0, 1, InetAddress.getByName("localhost"));
        return serverSocket.getLocalPort();
    }
}