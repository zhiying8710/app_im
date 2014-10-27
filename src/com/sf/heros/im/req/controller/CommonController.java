package com.sf.heros.im.req.controller;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.RespMsgPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.AskLoginRespMsg;
import com.sf.heros.im.common.bean.msg.KickedRespMsg;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.service.SessionService;


public abstract class CommonController {

    private static final Logger logger = Logger.getLogger(CommonController.class);

    private static final Map<Integer, CommonController> CONTROLLERS = new HashMap<Integer, CommonController>();

    private SessionService sessionService;

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

    public CommonController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public abstract void exec(Object msg, ChannelHandlerContext ctx, String sessionId);

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

    protected  final ReqMsg transfer(Object msg) {
        return (ReqMsg) msg;
    }

    protected final void writeAndFlush(Channel channel, RespMsg respMsg) {
        try {
            channel.writeAndFlush(ImUtils.getBuf(channel.alloc(), respMsg));
            logger.info("write resp msg " + respMsg);
        } catch (UnsupportedEncodingException e) {
        }
    }

    protected final void writeAndFlush(ChannelHandlerContext ctx, RespMsg respMsg) {
        try {
            ctx.writeAndFlush(ImUtils.getBuf(ctx.alloc(), respMsg));
            logger.info("write resp msg " + respMsg);
        } catch (UnsupportedEncodingException e) {
        }
    }

    protected final boolean checkAuth(Object msg, ChannelHandlerContext ctx, String sessionId) {
        ReqMsg reqMsg = transfer(msg);
        String reqSessionId = reqMsg.getSid();
        Session session = null;

        if (reqSessionId != null) {
            session = sessionService.get(reqSessionId);
            if (reqSessionId.equals(sessionId) && session != null) {
                if (Session.STATUS_KICKED == session.getStatus()) {
                    try {
                        RespMsgPublisher.publish(reqSessionId, new KickedRespMsg());
                    } catch (Exception e) {
                        logger.error("publish kicked msg to session(" + reqSessionId + ") user(" + session.getUserId() + ") error", e);
                    }
                    logger.info("user(" + session.getUserId() + ") which session(" + reqSessionId + ") is kicked.");
                    return false;
                }
                return true;
            } else {
                writeAndFlush(ctx.channel(), new AskLoginRespMsg());
                return false;
            }
        } else {
            writeAndFlush(ctx.channel(), new AskLoginRespMsg());
            return false;
        }
    }
}
