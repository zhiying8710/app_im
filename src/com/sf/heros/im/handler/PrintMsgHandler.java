package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.bean.msg.ReqMsg;

public class PrintMsgHandler extends SimpleChannelInboundHandler<ReqMsg> {

    private static final Logger logger = Logger.getLogger(PrintMsgHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReqMsg msg)
            throws Exception {
        logger.info("got msg : " + msg);
        ctx.fireChannelRead(msg);
    }

}
