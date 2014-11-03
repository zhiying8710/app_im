package com.sf.heros.im.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.udt.nio.NioUdtByteConnectorChannel;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import com.sf.heros.im.channel.util.ClientChannelIdUtil;
import com.sf.heros.im.common.Const;

public class UdtSocketChannel extends NioUdtByteConnectorChannel implements ClientChannel {

    private Long id;

    public UdtSocketChannel() {
        super();
        setId();
    }

    public UdtSocketChannel(Channel parent, SocketChannelUDT channelUDT) {
        super(parent, channelUDT);
        setId();
    }

    public UdtSocketChannel(SocketChannelUDT channelUDT) {
        super(channelUDT);
        setId();
    }

    public UdtSocketChannel(TypeUDT type) {
        super(type);
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
        group();
    }

    @Override
    public boolean group() {
        return ClientChannelGroup.add(this);
    }

    @Override
    public boolean ungroup() {
        return ClientChannelGroup.remove(this);
    }

    @Override
    public ChannelFuture disconnect() {
        ungroup();
        return super.disconnect();
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        ungroup();
        return super.disconnect(promise);
    }

}
