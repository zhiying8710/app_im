package com.sf.heros.im.test.handler;

import com.sf.heros.im.common.bean.msg.Resp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class PrintHandler extends SimpleChannelInboundHandler<Resp> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Resp msg)
            throws Exception {
        System.out.println(msg);
        ctx.fireChannelRead(msg);
    }

}
