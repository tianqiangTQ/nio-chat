package com.tq.client;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientThread extends Thread {
    private Selector selector;

    public ClientThread(Selector selector) {
        this.selector = selector;
    }


    @Override
    public void run() {
        try {
            while (true) {
                int select = selector.select();
                if (select == 0) {
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        // 读取内容
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        try {
                            socketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            System.out.println(UTF_8.decode(byteBuffer));

                            // 监听可读状态
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        } catch (Exception e) {
                            selectionKey.cancel();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
