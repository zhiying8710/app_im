package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sf.heros.im.common.AuthCheck;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.ReqMsg;
import com.sf.heros.im.common.RespMsg;
import com.sf.heros.im.common.Session;
import com.sf.heros.im.common.exception.AuthException;
import com.sf.heros.im.service.AuthService;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserStatusService;

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
        try {
            String userId = reqMsg.getUserId();
            String token = reqMsg.getToken();
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(token)) {
                throw new AuthException("userId or token can't be null or empty.");
            }
            String sessionId = getSessionId(ctx);
            Session session = sessionService.get(sessionId);
            if (session != null && session.getStatus() == Session.STATUS_KICKED) {
                sessionService.del(sessionId);
                ChannelFuture kickFuture = ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), new RespMsg(Const.RespMsgConst.TYPE_KICKED)));
                kickFuture.channel().closeFuture();
                ReferenceCountUtil.release(reqMsg);
                ReferenceCountUtil.release(msg);
                return;
            }
            AuthCheck checkRes = authService.check(userId, token);
            if (!checkRes.isIllegal()) {
                logger.warn("illegal connection, close it.");
                userStatusService.userOffline(userId);
                sessionService.del(sessionId);
                ctx.close();
            }
            if(checkRes.isPass()) { // pass check
                if (!checkRes.isOnline()) {
                    userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
                    session = new Session(sessionId, ctx.channel(), new Date().getTime(), Session.STATUS_ONLINE);
                    session.setAttr(Const.UserConst.SESSION_USER_ID_KEY, userId);
                    session.setAttr(Const.UserConst.SESSION_TOKEN_KEY, token);
                    sessionService.add(sessionId, session);

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
                                RespMsg respMsg = new RespMsg();
                                respMsg.setType(Const.RespMsgConst.TYPE_OFFLINE_MSG);
                                respMsg.setToData(Const.RespMsgConst.DATA_KEY_OFFLINE_MSGS, perOfflineMsgs);
                                respMsg.setToData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID, Const.CommonConst.SERVER_USER_ID + Const.CommonConst.KEY_SEP + new Date().getTime());
                                respMsg.setToData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, userId);
                                ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), respMsg));

                                String unAckRespMsgId = getUnAckMsgId(respMsg);
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
//                    userStatusService.userOnline(userId, token, sessionId);
                    sessionService.updatePingTime(sessionId);
                }
            } else { // illegal info
                String kSessionId = userStatusService.getSessionId(userId);
                if (Const.RedisKeyValConst.SINGEL_ERR_VAL.equals(kSessionId)) {

                    RespMsg respMsg = new RespMsg();
                    respMsg.setType(Const.RespMsgConst.TYPE_SERVER_ERR);
                    ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), reqMsg));

                }
                Session kickSession = sessionService.kick(kSessionId);

                userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
                if (kickSession != null) {
                    Channel kickChannel = kickSession.getChannel();
                    ChannelFuture kickFuture = kickChannel.writeAndFlush(ImUtils.getBuf(kickChannel.alloc(), new RespMsg(Const.RespMsgConst.TYPE_KICKED)));
                    kickFuture.channel().closeFuture();
                    sessionService.del(kSessionId);
                }
            }
            ctx.fireChannelRead(msg);
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
            ReferenceCountUtil.release(reqMsg);
            ReferenceCountUtil.release(msg);
        } finally {
        }
    }

}
