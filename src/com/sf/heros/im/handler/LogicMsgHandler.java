package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.ReqMsg;
import com.sf.heros.im.common.RespMsg;
import com.sf.heros.im.common.Session;
import com.sf.heros.im.common.UserInfo;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserInfoService;
import com.sf.heros.im.service.UserStatusService;

public class LogicMsgHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(LogicMsgHandler.class);

    private SessionService sessionService;
    private UserStatusService userStatusService;
    private UserInfoService userInfoService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;

    public LogicMsgHandler(SessionService sessionService,
            UserStatusService userStatusService, UserInfoService userInfoService, RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService) {
        super();
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
        this.userInfoService = userInfoService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffline(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        userOffline(ctx);
        super.channelUnregistered(ctx);
    }

    private void userOffline(ChannelHandlerContext ctx) {
        String sessionId = getSessionId(ctx);
        Session session = sessionService.get(sessionId);
        if (session != null) {
            logger.warn("channle(" + sessionId + ") is closed, user offline.");
            String userId = session.getAttr(Const.UserConst.SESSION_USER_ID_KEY).toString();
            userStatusService.userOffline(userId);
            sessionService.del(sessionId);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ReqMsg reqMsg = null;
        if (msg instanceof ReqMsg) {
            try {
                reqMsg = (ReqMsg) msg;
                String me = reqMsg.getUserId();
                UserInfo meInfo = null;
                if (StringUtils.isNotBlank(me)) {
                    meInfo = userInfoService.getById(me);
                }
                String to = null;
                if (reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, null) != null) {
                    to = reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID).toString();
                }
                String content = null;
                if (reqMsg.getFromData(Const.ReqMsgConst.DATA_CONTENT, null) != null) {
                    content = reqMsg.getFromData(Const.ReqMsgConst.DATA_CONTENT).toString();
                }
                RespMsg respMsg = new RespMsg();
                int type = reqMsg.getType();
                respMsg.setType(type);
                respMsg.setToData(Const.RespMsgConst.DATA_KEY_CONTENT, content);
                respMsg.setToData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID, me);
                respMsg.setToData(Const.RespMsgConst.DATA_KEY_FROM_USER_INFO, meInfo);
                respMsg.setToData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, to);
                switch (type) {
                case Const.ReqMsgConst.TYPE_STRING_MSG:
                case Const.ReqMsgConst.TYPE_VOICE_MSG:
                    boolean online = userStatusService.isOnline(to);
                    String toSessionId = userStatusService.getSessionId(to);
                    if (online) {
                        if (toSessionId == null) {
                            online = false;
                        } else {
                            Session session = sessionService.get(toSessionId);
                            if (session == null || session.getStatus() == Session.STATUS_OFFLINE) {
                                online = false;
                            } else {
                                Channel toChannel = session.getChannel();
                                if (toChannel == null || !toChannel.isRegistered()) {
                                    online = false;
                                }
                            }
                        }
                    }
                    if (online) {
                        Channel toChannel = sessionService.get(toSessionId).getChannel();
                        toChannel.writeAndFlush(ImUtils.getBuf(toChannel.alloc(), respMsg));
                        String unAckMsgId = getUnAckMsgId(respMsg);
                        respMsgService.saveUnAck(unAckMsgId, respMsg);
                        unAckRespMsgService.add(unAckMsgId);
                    } else {
                        userStatusService.userOffline(to);
                        respMsgService.saveOffline(to, respMsg);
                    }
                    break;
                case Const.ReqMsgConst.TYPE_LOGOUT:
                    String sessionId = userStatusService.getSessionId(me);
                    userStatusService.userOffline(me);
                    sessionService.del(sessionId);
                    ctx.close();
                    break;
                case Const.ReqMsgConst.TYPE_PING:
                case Const.ReqMsgConst.TYPE_ACK:
                default:
                    break;
                }
            } catch (Exception e) {
                logger.error("handle msg error, " + msg, e);
                RespMsg errAckMsg = new RespMsg(Const.RespMsgConst.TYPE_MSG_HANDLER_ERROR);
                errAckMsg.setToData(Const.RespAckMsgConst.DATA_KEY_REMARK, "reason: " + e.getMessage());
                errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_FROM_TIME, reqMsg.getTime());
                errAckMsg.setToData(Const.RespAckMsgConst.DATA_MIME_USERID, reqMsg.getUserId());
                errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, "null"));
                errAckMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TYPE, reqMsg.getType() + "");
                ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), errAckMsg));
            } finally {
                if (reqMsg != null) {
                    ReferenceCountUtil.release(reqMsg);
                }
                ReferenceCountUtil.release(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
