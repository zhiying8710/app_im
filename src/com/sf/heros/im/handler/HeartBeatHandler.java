package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.ReqPingRespMsg;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

@Sharable
@Deprecated
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
            Session session = sessionService.get(sessionId);
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                if (session == null) {
                    ctx.close();
                    logger.info("channel(" + sessionId + ") is write idle overtime, and user haven't login, close it.");
                    return;
                }
                writeAndFlush(ctx.channel(), new ReqPingRespMsg());
                logger.info("channel(" + sessionId + ") is write idle overtime, send a msg to req ping.");
            }

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (session == null || session.overtime()) {
                    if (session != null) {
                        userStatusService.userOffline(session.getUserId());
                    }
                    sessionService.del(sessionId);
                    ctx.close();
                    logger.warn("channel(" + sessionId + ") is read idle overtime, and session is null or overtime, user is offline.");
                }
            }

            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                logger.warn("channel(" + sessionId + ") is all idle overtime, user is offline.");
                if (session != null) {
                    userStatusService.userOffline(session.getUserId());
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
