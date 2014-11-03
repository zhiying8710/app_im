package com.sf.heros.im.channel;

import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.log4j.Logger;

public class TcpServerSocketChannel extends NioServerSocketChannel implements ServerChannel {

    private static final Logger logger = Logger.getLogger(TcpServerSocketChannel.class);

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = javaChannel().accept();

        try {
            if (ch != null) {
                buf.add(new TcpSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            logger.warn("Failed to create a new channel from an accepted socket.", t);

            try {
                ch.close();
            } catch (Throwable t2) {
                logger.warn("Failed to close a socket.", t2);
            }
        }

        return 0;
    }

}
