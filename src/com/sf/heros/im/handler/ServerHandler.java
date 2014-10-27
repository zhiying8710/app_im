package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.Counter;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.ReqPingRespMsg;
import com.sf.heros.im.req.controller.AckController;
import com.sf.heros.im.req.controller.ChatController;
import com.sf.heros.im.req.controller.CommonController;
import com.sf.heros.im.req.controller.LoginController;
import com.sf.heros.im.req.controller.LogoutController;
import com.sf.heros.im.req.controller.PingController;
import com.sf.heros.im.service.AuthService;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserInfoService;
import com.sf.heros.im.service.UserStatusService;

@Sharable
public class ServerHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(ServerHandler.class);

    private SessionService sessionService;
    private UserStatusService userStatusService;

    public ServerHandler(AuthService authService, SessionService sessionService,
            UserStatusService userStatusService, UserInfoService userInfoService, RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService) {
        super();
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
        CommonController.add(Const.ReqMsgConst.TYPE_ACK, new AckController(sessionService, respMsgService, unAckRespMsgService));
        CommonController.add(Const.ReqMsgConst.TYPE_LOGIN, new LoginController(authService, userStatusService, sessionService, respMsgService, unAckRespMsgService));
        CommonController.add(Const.ReqMsgConst.TYPE_LOGOUT, new LogoutController(userStatusService, sessionService));
        CommonController.add(Const.ReqMsgConst.TYPE_PING, new PingController(sessionService));
        ChatController chatController = new ChatController(userStatusService, sessionService, respMsgService, unAckRespMsgService, userInfoService);
        CommonController.add(Const.ReqMsgConst.TYPE_STRING_MSG, chatController);
        CommonController.add(Const.ReqMsgConst.TYPE_VOICE_MSG, chatController);

        final ScheduledExecutorService counterExecutor = Executors.newSingleThreadScheduledExecutor();
        counterExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                logger.info("current connections count : " + Counter.getConns() + ", current login user count : " + Counter.getOnlines());
            }
        }, 1, 10, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                counterExecutor.shutdownNow();
            }
        }));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Counter.incrConnsAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffline(ctx);
        Counter.decrConnsAndGet();
        super.channelInactive(ctx);
    }

    private void userOffline(ChannelHandlerContext ctx) {
        String sessionId = getSessionId(ctx);
        Session session = sessionService.get(sessionId);
        if (session != null) {
            String userId = session.getUserId();
            userStatusService.userOffline(userId);
            sessionService.del(sessionId);
            logger.warn("channle(" + sessionId + ") is closed, user offline.");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ReqMsg reqMsg = null;
        if (msg instanceof ReqMsg) {
            try {
                reqMsg = (ReqMsg) msg;
                int type = reqMsg.getType();
                CommonController.get(type).exec(msg, ctx, getSessionId(ctx));
            } catch (Exception e) {
                logger.error("handler reqMsg(" + reqMsg.toJson() + ") error", e);
            } finally {
                releaseObjs(reqMsg, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
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
