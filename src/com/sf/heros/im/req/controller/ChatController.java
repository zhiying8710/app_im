package com.sf.heros.im.req.controller;

import io.netty.channel.ChannelHandlerContext;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.UserInfo;
import com.sf.heros.im.common.bean.msg.AckResp;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.bean.msg.StringResp;
import com.sf.heros.im.common.bean.msg.VoiceResp;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserInfoService;
import com.sf.heros.im.service.UserStatusService;

public class ChatController extends CommonController {

    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;
    private UserInfoService userInfoService;

    public ChatController(UserStatusService userStatusService, SessionService sessionService,
            RespMsgService respMsgService,
            UnAckRespMsgService unAckRespMsgService, UserInfoService userInfoService) {
        super(sessionService, userStatusService);

        this.sessionService = sessionService;
        this.userInfoService = userInfoService;
        this.userStatusService = userStatusService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void exec(Object msg, ChannelHandlerContext ctx, Long sessionId) {
        sessionService.updatePingTime(sessionId);

        if (!checkAuth(msg, ctx, sessionId)) {
            return;
        }

        Req reqMsg = transfer(msg);
        Resp ackMsg = new AckResp(sessionId, reqMsg.getFromData(Const.ReqConst.DATA_MSG_NO, "0000").toString(), sessionService.get(sessionId).getUserId(), reqMsg.getFromData(Const.ReqConst.DATA_TO_USERID, "null").toString());
        writeAndFlush(ctx.channel(), ackMsg);

        String content = reqMsg.getFromData(Const.ReqConst.DATA_CONTENT).toString();
        String from = sessionService.get(reqMsg.getSid()).getUserId();
        UserInfo fromInfo = userInfoService.getById(from);
        String to = reqMsg.getFromData(Const.ReqConst.DATA_TO_USERID).toString();
        Resp respMsg = null;
        int type = reqMsg.getType();
        if (type == Const.ReqConst.TYPE_STRING_MSG) {
            respMsg = new StringResp(sessionId, content, from, fromInfo, to);
        }
        if (type == Const.ReqConst.TYPE_VOICE_MSG) {
            respMsg = new VoiceResp(sessionId, content, from, fromInfo, to);
        }
        if (respMsg == null) {
            return;
        }
        boolean online = userStatusService.isOnline(to);
        Long toSessionId = userStatusService.getSessionId(to);

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
            String msgNo = respMsg.getMsgNo();
            respMsgService.saveUnAck(respMsg.getMsgNo(), respMsg);
            unAckRespMsgService.add(msgNo);
            online = RespPublisher.publish(toSessionId, respMsg);
        }
        if (!online) {
            userStatusService.userOffline(to);
            respMsgService.saveOffline(to, respMsg);
        }

    }

}
