package com.sf.heros.im.channel;

import io.netty.channel.Channel;
import io.netty.channel.udt.nio.NioUdtByteConnectorChannel;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import com.sf.heros.im.common.ImUtils;

public class UdtSocketChannel extends NioUdtByteConnectorChannel {

    private String id;

    public UdtSocketChannel() {
        super();
    }

    public UdtSocketChannel(Channel parent, SocketChannelUDT channelUDT) {
        super(parent, channelUDT);
    }

    public UdtSocketChannel(SocketChannelUDT channelUDT) {
        super(channelUDT);
    }

    public UdtSocketChannel(TypeUDT type) {
        super(type);
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
