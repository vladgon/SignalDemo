package com.demo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by vova on 12/21/20
 */
@RequiredArgsConstructor
@Slf4j
public class ProxyTunnel {
    private final String host;
    private final int port;

    @SneakyThrows
    public void listen() {
        final ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(8081);
        log.debug("Listening on {}", 8081);
        for (Socket srcSocket = serverSocket.accept(); srcSocket != null; srcSocket = serverSocket.accept()) {
            log.debug("Got connection {}", srcSocket);
            forward(srcSocket, host, port);
        }
    }

    @SneakyThrows
    public void forward(final Socket srcSock, final String host, int port) {
        log.debug("Forwarding to {}:{}", host, port);
        final Socket dstSock = SocketFactory.getDefault().createSocket(host, port);
        final InputStream srcInputStream = srcSock.getInputStream();
        final OutputStream srcOutputStream = srcSock.getOutputStream();

        final InputStream dstInputStream = dstSock.getInputStream();
        final OutputStream dstOutputStream = dstSock.getOutputStream();
        CompletableFuture
                .allOf(CompletableFuture.runAsync(() -> forward(srcInputStream, dstOutputStream, "src->dst")),
                       CompletableFuture.runAsync(() -> forward(dstInputStream, srcOutputStream, "dst->src")))
                .thenRun(() -> {
                    try {
                        log.debug("Forwarding {}:{} Complete", host, port);
                        dstSock.close();
                        srcSock.close();
                        log.debug("Sockets are closed");
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                })
                .get(2, TimeUnit.MINUTES);
    }

    @SneakyThrows
    public void forward(InputStream inputStream, OutputStream outputStream, String description) {
        log.debug("Copying stream begin {}", description);
        try {
            for (int b = inputStream.read(); b != -1; b = inputStream.read()) {
                outputStream.write(b);
                outputStream.flush();
            }
            log.debug("Copy Stream flush {}", description);
        } catch (Exception e) {
            log.error("Server Error Response {}", description, e);
        }
    }
}
