package com.gbdmf.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NioAsyncServer {
    public static void main(String[] args) throws IOException {
        // 1. 创建 selector 和 ServerSocketChannel
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(9000));
        serverChannel.configureBlocking(false);  // 设置为非阻塞模式
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("NIO Async Server started on port 8080...");

        while (true) {
            selector.select();  // 阻塞直到有就绪的 Channel
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();  // 处理后移除

                if (key.isAcceptable()) {
                    handleAccept(serverChannel, selector);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("Accepted new connection from: " + client.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = client.read(buffer);

        if (read == -1) {
            client.close();
            System.out.println("Client disconnected");
            return;
        }

        buffer.flip();
        String request = new String(buffer.array(), 0, buffer.limit());
        System.out.println("Received: " + request);

        // 简单回应
        String response = "Hello from NIO server!\n";
        client.write(ByteBuffer.wrap(response.getBytes()));
    }
}
