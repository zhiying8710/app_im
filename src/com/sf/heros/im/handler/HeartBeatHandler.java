package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.RespMsg;
import com.sf.heros.im.common.Session;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class HeartBeatHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(HeartBeatHandler.class);

    private UserStatusService userStatusService;
    private SessionService sessionService;

    public HeartBeatHandler(UserStatusService userStatusService, SessionService sessionService) {
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {

        boolean fire = true;

        if (evt instanceof IdleStateEvent) {
            fire = false;
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            String sessionId = getSessionId(ctx);
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                logger.info("channel(" + sessionId + ") is write idle overtime, send a msg to req ping.");
                ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), new RespMsg(Const.RespMsgConst.TYPE_REQ_PING)));
            }

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                Session session = sessionService.get(sessionId);
                if (session == null || session.overtime()) {
                    logger.warn("channel(" + sessionId + ") is read idle overtime, and session is null or overtime, user is offline.");
                    if (session != null) {
                        userStatusService.userOffline(session.getAttr(Const.UserConst.SESSION_USER_ID_KEY).toString());
                    }
                    sessionService.del(sessionId);
                    ctx.close();
                }
            }

            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                logger.warn("channel(" + sessionId + ") is all idle overtime, user is offline.");
                Session session = sessionService.get(sessionId);
                if (session != null) {
                    userStatusService.userOffline(session.getAttr(Const.UserConst.SESSION_USER_ID_KEY).toString());
                }
                sessionService.del(sessionId);
                ctx.close();
            }
        }
        if (fire) {
            super.userEventTriggered(ctx, evt);
        }
    }

}
