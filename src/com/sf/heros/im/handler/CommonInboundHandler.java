package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.bean.msg.Resp;

public class CommonInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(CommonInboundHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().flush();
        ctx.flush();
    }

    public void releaseObjs(Object... objs) {
        if (objs != null) {
            for (Object obj : objs) {
                if (obj != null) {
                    ReferenceCountUtil.release(obj);
                    obj = null;
                }

            }

        }
    }

    public void writeAndFlush(Channel channel, Resp respMsg) {
        try {
//            channel.writeAndFlush(ImUtils.getBuf(channel.alloc(), respMsg));
            channel.writeAndFlush(respMsg);
            logger.info("write resp msg " + respMsg);
        } catch (Exception e) {
        }
    }

    public void writeAndFlush(ChannelHandlerContext ctx, Resp respMsg) {
        writeAndFlush(ctx.channel(), respMsg);
    }

}
