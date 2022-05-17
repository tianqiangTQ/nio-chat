package com.tq.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatClient {

    public static void startClient(String name) throws IOException {
        // 连接服务端
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(8888));
        socketChannel.configureBlocking(false);

        // 接收服务端消息
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        new ClientThread(selector).start();

        // 发送消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            socketChannel.write(UTF_8.encode(String.format("%s : %s", name, msg)));
        }
    }

}
