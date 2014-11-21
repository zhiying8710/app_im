package com.sf.heros.im.channel;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import com.sf.heros.im.channel.util.ClientChannelIdUtil;
import com.sf.heros.im.common.Const;

public class TcpSocketChannel extends NioSocketChannel implements ClientChannel {

    private Long id;

    public TcpSocketChannel() {
        super();
        setId();
    }

    public TcpSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
        setId();
    }

    public TcpSocketChannel(SelectorProvider provider) {
        super(provider);
        setId();
    }

    public TcpSocketChannel(SocketChannel socket) {
        super(socket);
        setId();
    }

    @Override
    public Long getId() {
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
        this.id = ClientChannelIdUtil.getId();
        if (this.id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            throw new ExceptionInInitializerError(this + " init error.");
        }
    }

    @Override
    public boolean group() {
        return ClientChannelGroup.add(this);
    }

    @Override
    public boolean ungroup() {
        return ClientChannelGroup.remove(this);
    }
}
