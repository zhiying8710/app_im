package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.log4j.Logger;

public class FinalHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(FinalHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("caught a previous handlers logic unhandle exception, log it.", cause);

    }

}
