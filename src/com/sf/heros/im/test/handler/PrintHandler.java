package com.sf.heros.im.test.handler;

import com.sf.heros.im.common.bean.msg.RespMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PrintHandler extends SimpleChannelInboundHandler<RespMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RespMsg msg)
            throws Exception {
        System.out.println(msg);
        ctx.fireChannelRead(msg);
    }

}
