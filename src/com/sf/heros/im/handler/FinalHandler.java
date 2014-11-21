package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;

@Sharable
public class FinalHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(FinalHandler.class);

    public FinalHandler() {
    	super(null, null);
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("caught a previous handlers logic unhandle exception, log it.", cause);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        releaseObjs(msg);

    }



}
