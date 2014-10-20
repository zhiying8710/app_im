package com.sf.heros.im.channel;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import com.sf.heros.im.common.ImUtils;

public class TcpSocketChannel extends NioSocketChannel {

    private String id;

    public TcpSocketChannel() {
        super();
    }

    public TcpSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    public TcpSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public TcpSocketChannel(SocketChannel socket) {
        super(socket);
    }

    public String getId() {
        if (id == null) {
            synchronized (this) {
                if (id == null) {
                    setId();
                }
            }
        }
        return id;
    }

    private void setId() {
        this.id = ImUtils.getUniqueChannelId(this.remoteAddress().getAddress().getHostAddress());
    }

}
