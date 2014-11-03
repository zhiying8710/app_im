package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.bean.msg.Req;

@Sharable
public class PrintMsgHandler extends SimpleChannelInboundHandler<Req> {

    private static final Logger logger = Logger.getLogger(PrintMsgHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Req msg)
            throws Exception {
        logger.info("got msg : " + msg);
        ctx.fireChannelRead(msg);
    }

}
