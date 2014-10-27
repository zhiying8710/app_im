package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespMsgPublisher;
import com.sf.heros.im.common.bean.AuthCheck;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.AskLoginRespMsg;
import com.sf.heros.im.common.bean.msg.KickedRespMsg;
import com.sf.heros.im.common.bean.msg.LoginRespMsg;
import com.sf.heros.im.common.bean.msg.OfflineMsgsRespMsg;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.common.bean.msg.ServErrRespMsg;
import com.sf.heros.im.service.AuthService;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserStatusService;

@Sharable
@Deprecated
public class AuthHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(AuthHandler.class);

    private AuthService authService;
    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;

    public AuthHandler(AuthService authService, UserStatusService userStatusService, SessionService sessionService, RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService) {
        super();
        this.authService = authService;
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (!(msg instanceof ReqMsg)) {
            ctx.fireChannelRead(msg);
            return;
        }
        ReqMsg reqMsg = (ReqMsg) msg;
        int type = reqMsg.getType();
        switch (type) {
        case Const.ReqMsgConst.TYPE_ACK:
        case Const.ReqMsgConst.TYPE_PING:
            sessionService.updatePingTime(getSessionId(ctx));
        case Const.ReqMsgConst.TYPE_LOGOUT:
            ctx.fireChannelRead(msg);
            return;
        }

        try {

            String sessionId = reqMsg.getSid();
            if (sessionId != null) {
                Session session = sessionService.get(sessionId);
                if (sessionId.equals(getSessionId(ctx)) && session != null) {
                    if (Session.STATUS_KICKED == session.getStatus()) {
                        RespMsgPublisher.publish(sessionId, new KickedRespMsg());
                        releaseObjs(reqMsg, msg);
                        logger.info("user(" + session.getUserId() + ") which session(" + sessionId + ") is kicked.");
                        return;
                    }
                    sessionService.updatePingTime(sessionId);
                    ctx.fireChannelRead(reqMsg);
                    return;
                } else {
                    writeAndFlush(ctx.channel(), new AskLoginRespMsg());
                    releaseObjs(reqMsg, msg);
                    return;
                }
            }
//            String sessionId = reqMsg.getSid();
//            if (sessionId != null) {
//                Session session = sessionService.get(sessionId);
//                if (sessionId.equals(getSessionId(ctx)) && session != null) {
//                    if (Session.STATUS_KICKED == session.getStatus()) {
//                        Channel kickChannel = session.getChannel();
//                        writeAndFlush(kickChannel, new KickedRespMsg());
//                        releaseObjs(reqMsg, msg);
//                        logger.info("user(" + session.getUserId() + ") which session(" + sessionId + ") is kicked.");
//                        return;
//                    }
//                    sessionService.updatePingTime(sessionId);
//                    ctx.fireChannelRead(reqMsg);
//                    return;
//                } else {
//                    writeAndFlush(ctx.channel(), new AskLoginRespMsg());
//                    releaseObjs(reqMsg, msg);
//                    return;
//                }
//            }

            String userId = reqMsg.getFromData(Const.ReqMsgConst.DATA_AUTH_USERID, "").toString();
            String token = reqMsg.getFromData(Const.ReqMsgConst.DATA_AUTH_TOKEN, "").toString();
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(token)) {
                writeAndFlush(ctx.channel(), new AskLoginRespMsg());
                releaseObjs(reqMsg, msg);
                return;
            }

            AuthCheck checkRes = authService.check(userId, token);
            if (!checkRes.isIllegal()) {
                userStatusService.userOffline(userId);
                sessionService.del(sessionId);
                ctx.close();
                releaseObjs(reqMsg, msg);
                logger.warn("illegal connection, close it.");
            }

            sessionId = getSessionId(ctx);
            RespMsg loginRespMsg = new LoginRespMsg(sessionId);
            if (checkRes.isPass()) {
                if (!checkRes.isOnline()) {
                    userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
                    Session session = new Session(sessionId, userId, token, new Date().getTime(), Session.STATUS_ONLINE);
                    sessionService.add(sessionId, session);
                    writeAndFlush(ctx.channel(), loginRespMsg);
                    logger.info("user(" + userId + ") login, return session id " + sessionId);

                    List<String> offlineMsgs = respMsgService.getOfflines(userId);
                    if (offlineMsgs != null && !offlineMsgs.isEmpty()) {
                        LinkedList<String> linkedOfflineMsgs = new LinkedList<String>();
                        linkedOfflineMsgs.addAll(offlineMsgs);
                        int size = offlineMsgs.size();
                        int page = (size % Const.CommonConst.OFFLINE_MSG_SEND_PER_SIZE) == 0 ? (size / Const.CommonConst.OFFLINE_MSG_SEND_PER_SIZE) : (size / Const.CommonConst.OFFLINE_MSG_SEND_PER_SIZE + 1);
                        int k = 0;
                        List<String> perOfflineMsgs = new ArrayList<String>();
                        for (int i = 0; i < page; i++) {
                            if (k == Const.CommonConst.OFFLINE_MSG_SEND_PER_SIZE) {
                                RespMsg respMsg = new OfflineMsgsRespMsg(perOfflineMsgs, Const.CommonConst.SERVER_USER_ID + Const.CommonConst.KEY_SEP + new Date().getTime(), userId);
                                writeAndFlush(ctx.channel(), respMsg);

                                String unAckRespMsgId = respMsg.getUnAckMsgId();
                                respMsgService.saveUnAck(unAckRespMsgId, respMsg);
                                unAckRespMsgService.add(unAckRespMsgId);

                                perOfflineMsgs = new ArrayList<String>();
                                k = 0;
                            }
                            perOfflineMsgs.add(linkedOfflineMsgs.pop());
                            k ++;
                        }

                    }

                } else {
                    String kSessionId = userStatusService.getSessionId(userId);
                    if (Const.RedisConst.SINGEL_ERR_VAL.equals(kSessionId)) {

                        RespMsg respMsg = new ServErrRespMsg();
                        writeAndFlush(ctx.channel(), respMsg);
                        releaseObjs(respMsg);
                        return;
                    }
                    Session kickSession = sessionService.kick(kSessionId);

                    if (kickSession != null) {
                        RespMsgPublisher.publish(kSessionId, new KickedRespMsg());
                        logger.info("user(" + userId + ") is login in more than onece, kick the first login.");
                    }
//                    if (kickSession != null) {
//                        Channel kickChannel = kickSession.getChannel();
//                        writeAndFlush(kickChannel, new KickedRespMsg());
//                        logger.info("user(" + userId + ") is login in more than onece, kick the first login.");
//                    }
                    userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
                    writeAndFlush(ctx.channel(), loginRespMsg);
                }
            }

            switch (type) {
            case Const.ReqMsgConst.TYPE_STRING_MSG:
            case Const.ReqMsgConst.TYPE_VOICE_MSG:
                ctx.fireChannelRead(msg);
                break;
            default:
                releaseObjs(reqMsg, msg);
                break;
            }
        } catch (Exception e) {
            logger.error("check user auth error.", e);
//            RespMsg errAckMsg = new RespMsg(Const.RespMsgConst.TYPE_MSG_HANDLER_ERROR);
//            errAckMsg.setToData(Const.RespAckMsgConst.DATA_KEY_REMARK, "reason: " + e.getMessage());
//            errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_FROM_TIME, reqMsg.getTime());
//            errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_FROM_USERID, reqMsg.getUserId());
//            errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, "null"));
//            errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TYPE, reqMsg.getType() + "");
//            ChannelFuture errFuture = ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), errAckMsg));
//            errFuture.channel().closeFuture();
            releaseObjs(reqMsg, msg);
        } finally {
        }
    }

}
