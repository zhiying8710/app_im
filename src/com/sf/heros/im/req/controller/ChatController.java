package com.sf.heros.im.req.controller;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespMsgPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.UserInfo;
import com.sf.heros.im.common.bean.msg.AckRespMsg;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.common.bean.msg.StringRespMsg;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserInfoService;
import com.sf.heros.im.service.UserStatusService;

public class ChatController extends CommonController {

    private static final Logger logger = Logger.getLogger(ChatController.class);

    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;
    private UserInfoService userInfoService;

    public ChatController(UserStatusService userStatusService, SessionService sessionService,
            RespMsgService respMsgService,
            UnAckRespMsgService unAckRespMsgService, UserInfoService userInfoService) {
        super(sessionService);

        this.sessionService = sessionService;
        this.userInfoService = userInfoService;
        this.userStatusService = userStatusService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void exec(Object msg, ChannelHandlerContext ctx, String sessionId) {
        sessionService.updatePingTime(sessionId);

        if (!checkAuth(msg, ctx, sessionId)) {
            return;
        }

        ReqMsg reqMsg = transfer(msg);
        RespMsg ackMsg = new AckRespMsg(reqMsg.getTime(), sessionService.get(sessionId).getUserId(), reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, "null").toString(), reqMsg.getType());
        writeAndFlush(ctx.channel(), ackMsg);

        String content = reqMsg.getFromData(Const.ReqMsgConst.DATA_CONTENT).toString();
        String from = sessionService.get(reqMsg.getSid()).getUserId();
        UserInfo fromInfo = userInfoService.getById(from);
        String to = reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID).toString();
        RespMsg respMsg = new StringRespMsg(content, from, fromInfo, to);
        boolean online = userStatusService.isOnline(to);
        String toSessionId = userStatusService.getSessionId(to);

        if (online) {
            if (toSessionId == null) {
                online = false;
            } else {
                Session session = sessionService.get(toSessionId);
                if (session == null || session.getStatus() == Session.STATUS_OFFLINE) {
                    online = false;
                }
            }
        }
        if (online) {
            String unAckMsgId = respMsg.getUnAckMsgId();
            respMsgService.saveUnAck(unAckMsgId, respMsg);
            unAckRespMsgService.add(unAckMsgId);
            try {
                RespMsgPublisher.publish(toSessionId, respMsg);
            } catch (Exception e) {
                logger.error("publish msg to session(" + toSessionId + ") user(" + to + ") error.", e);
            }
        } else {
            userStatusService.userOffline(to);
            respMsgService.saveOffline(to, respMsg);
        }

    }

}
