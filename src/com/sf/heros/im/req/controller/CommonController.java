package com.sf.heros.im.req.controller;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sf.heros.im.channel.listener.WriteAndFlushFailureListener;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.AckResp;
import com.sf.heros.im.common.bean.msg.AskLoginResp;
import com.sf.heros.im.common.bean.msg.KickedResp;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.server.ShutdownHookUtils;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;


public abstract class CommonController {

    private static final Logger logger = Logger.getLogger(CommonController.class);

    private static final Map<Integer, CommonController> CONTROLLERS = new HashMap<Integer, CommonController>();

    private SessionService sessionService;
    private UserStatusService userStatusService;

    static {
        ShutdownHookUtils.shutdownControllers();
    }

    public CommonController(SessionService sessionService, UserStatusService userStatusService) {
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
    }

    public abstract void exec(Object msg, Long sessionId, boolean needAck) throws Exception ;

    public void release() {
        logger.info(this.getClass() + " released.");
    }

    public final static void add(int idx, CommonController controller) {
        if (controller == null) {
            throw new NullPointerException("the controller which will be added to CommonController<T extends RespMsg>.CONTROLLERS can not be null.");
        }
        if (CONTROLLERS.get(idx) != null) {
            throw new IllegalStateException("in CommonController<T extends RespMsg>.CONTROLLERS has already have a controller on index " + idx);
        }
        CONTROLLERS.put(idx, controller);
    }

    public final static CommonController get(int idx) {
        CommonController controller = CONTROLLERS.get(idx);
        if (controller == null) {
            throw new NullPointerException("the controller which index " + idx + " in CommonController<T extends RespMsg>.CONTROLLERS is null.");
        }
        return controller;
    }

    protected  final Req transfer(Object msg) {
        return (Req) msg;
    }

    protected final void writeAndFlush(Channel channel, Resp respMsg) {
        try {
//            channel.writeAndFlush(ImUtils.getBuf(channel.alloc(), respMsg));
            channel.writeAndFlush(respMsg).addListener(new WriteAndFlushFailureListener(sessionService, userStatusService));
            logger.info("write resp msg " + respMsg);
        } catch (Exception e) {
        }
    }

    protected final void writeAndFlush(ChannelHandlerContext ctx, Resp respMsg) {
        writeAndFlush(ctx.channel(), respMsg);
    }

    public final boolean checkAuth(Object msg, Long sessionId) throws Exception {
        Req reqMsg = transfer(msg);
        Long reqSessionId = reqMsg.getSid();
        Session session = null;

        if (reqSessionId != null && reqSessionId.longValue() != Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            try {
                session = sessionService.get(reqSessionId);
            } catch (Exception e) {
                logger.error("get session err.", e);
                return false;
            }
            if (reqSessionId.equals(sessionId) && session != null) {
                if (Session.STATUS_KICKED == session.getStatus()) {
                    try {
                        RespPublisher.publish(reqSessionId, session.getServerId(), new KickedResp(reqSessionId));
                        logger.info("user(" + session.getUserId() + ") which session(" + reqSessionId + ") is kicked.");
                    } catch (Exception e) {
                        logger.error("publish kick msg error.", e);
                    }
                    return false;
                }
                return true;
            } else {
                RespPublisher.publish(reqSessionId, session.getServerId(), new AskLoginResp());
                return false;
            }
        } else {
            return false;
        }
    }

    protected void ack(Long sessionId, String serverId, String msgNo, String to, boolean needAck)
            throws Exception {
        if (!needAck) {
            return;
        }
        Resp ackMsg = new AckResp(sessionId, msgNo, sessionService.get(sessionId).getUserId(), to);
        RespPublisher.publish(sessionId, serverId, ackMsg);
    }

    public static Collection<CommonController> getAll() {
        return CONTROLLERS.values();
    }

}
