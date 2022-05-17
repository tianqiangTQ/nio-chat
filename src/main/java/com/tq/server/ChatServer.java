package com.tq.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatServer {

    public static void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8888));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端启动成功");

        while (true) {
            int select = selector.select();
            if (select == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();

                if (selectionKey.isAcceptable()) {
                    doAccept(selector, serverSocketChannel);
                }
                if (selectionKey.isReadable()) {
                    doRead(selector, selectionKey);
                }
            }
        }
    }

    private static void doRead(Selector selector, SelectionKey selectionKey) throws IOException {
        SocketChannel thisChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            thisChannel.read(byteBuffer);
            // 切换读模式
            byteBuffer.flip();
            String msg = UTF_8.decode(byteBuffer).toString();

            // 再次注册到selector
            thisChannel.register(selector, SelectionKey.OP_READ);

            if (msg.length() > 0) {
                System.out.println(msg);
                castOtherClient(msg, selector, thisChannel);
            }
        } catch (Exception e) {
            selectionKey.cancel();
        }

    }

    private static void castOtherClient(String msg, Selector selector, SocketChannel thisChannel) throws IOException {
        Set<SelectionKey> selectionKeys = selector.keys();
        for (SelectionKey selectionKey : selectionKeys) {
            SelectableChannel channel = selectionKey.channel();
            if (channel instanceof SocketChannel && channel != thisChannel) {
                SocketChannel socketChannel = (SocketChannel) channel;
                socketChannel.write(UTF_8.encode(msg));
            }
        }
    }

    private static void doAccept(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        socketChannel.write(StandardCharsets.UTF_8.encode("欢迎进入tq的聊天室"));
    }

    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
