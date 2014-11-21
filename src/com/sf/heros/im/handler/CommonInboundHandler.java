package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.apache.log4j.Logger;

import com.sf.heros.im.channel.listener.WriteAndFlushFailureListener;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class CommonInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(CommonInboundHandler.class);

    private SessionService sessionService;
    private UserStatusService userStatusService;

    public CommonInboundHandler(SessionService sessionService, UserStatusService userStatusService) {
    	this.sessionService = sessionService;
    	this.userStatusService = userStatusService;
	}

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
            channel.writeAndFlush(respMsg).addListener(new WriteAndFlushFailureListener(sessionService, userStatusService));
            logger.info("write resp msg " + respMsg);
        } catch (Exception e) {
        }
    }

    public void writeAndFlush(ChannelHandlerContext ctx, Resp respMsg) {
        writeAndFlush(ctx.channel(), respMsg);
    }

}

