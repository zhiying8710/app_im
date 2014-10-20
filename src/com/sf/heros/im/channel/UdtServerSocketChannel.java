package com.sf.heros.im.channel;

import io.netty.channel.udt.nio.NioUdtByteAcceptorChannel;

import java.util.List;

import com.barchart.udt.nio.SocketChannelUDT;

public class UdtServerSocketChannel extends NioUdtByteAcceptorChannel {

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        final SocketChannelUDT channelUDT = javaChannel().accept();
        if (channelUDT == null) {
            return 0;
        } else {
            buf.add(new UdtSocketChannel(this, channelUDT));
            return 1;
        }
    }

}
