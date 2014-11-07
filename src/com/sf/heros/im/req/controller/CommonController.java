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
import com.sf.heros.im.common.bean.msg.AskLoginResp;
import com.sf.heros.im.common.bean.msg.KickedResp;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;


public abstract class CommonController {

    private static final Logger logger = Logger.getLogger(CommonController.class);

    private static final Map<Integer, CommonController> CONTROLLERS = new HashMap<Integer, CommonController>();

    private SessionService sessionService;
    private UserStatusService userStatusService;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                Collection<CommonController> controllers = CONTROLLERS.values();
                for (CommonController controller : controllers) {
                    if (controller != null) {
                        controller.release();
                    }
                }
            }
        }));
    }

    public CommonController(SessionService sessionService, UserStatusService userStatusService) {
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
    }

    public abstract void exec(Object msg, ChannelHandlerContext ctx, Long sessionId);

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

    protected final boolean checkAuth(Object msg, ChannelHandlerContext ctx, Long sessionId) {
        Req reqMsg = transfer(msg);
        Long reqSessionId = reqMsg.getSid();
        Session session = null;

        if (reqSessionId != null && reqSessionId.longValue() != Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            session = sessionService.get(reqSessionId);
            if (reqSessionId.equals(sessionId) && session != null) {
                if (Session.STATUS_KICKED == session.getStatus()) {
                    if (!RespPublisher.publish(reqSessionId, new KickedResp(reqSessionId))) {
                        return false;
                    }
                    logger.info("user(" + session.getUserId() + ") which session(" + reqSessionId + ") is kicked.");
                    return false;
                }
                return true;
            } else {
                writeAndFlush(ctx.channel(), new AskLoginResp());
                return false;
            }
        } else {
            writeAndFlush(ctx.channel(), new AskLoginResp());
            return false;
        }
    }
}
