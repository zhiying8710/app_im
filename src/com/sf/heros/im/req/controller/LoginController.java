package com.sf.heros.im.req.controller;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sf.heros.im.channel.util.ClientChannelIdUtil;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespMsgPublisher;
import com.sf.heros.im.common.bean.AuthCheck;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.AskLoginResp;
import com.sf.heros.im.common.bean.msg.KickedResp;
import com.sf.heros.im.common.bean.msg.LoginResp;
import com.sf.heros.im.common.bean.msg.OfflineMsgsResp;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.service.AuthService;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserStatusService;

public class LoginController extends CommonController {

    private static final Logger logger = Logger.getLogger(CommonController.class);

    private AuthService authService;
    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;

    public LoginController(AuthService authService,
            UserStatusService userStatusService, SessionService sessionService,
            RespMsgService respMsgService,
            UnAckRespMsgService unAckRespMsgService) {
        super(sessionService);
        this.authService = authService;
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void exec(Object msg, ChannelHandlerContext ctx, Long sessionId) {
        sessionService.updatePingTime(sessionId);

        Req reqMsg = transfer(msg);
        String userId = reqMsg.getFromData(Const.ReqConst.DATA_AUTH_USERID, "").toString();
        String token = reqMsg.getFromData(Const.ReqConst.DATA_AUTH_TOKEN, "").toString();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(token)) {
            writeAndFlush(ctx.channel(), new AskLoginResp(Const.ProtocolConst.EMPTY_SESSION_ID));
            return;
        }

        AuthCheck checkRes = authService.check(userId, token);
        if (!checkRes.isIllegal()) {
            userStatusService.userOffline(userId);
            sessionService.del(sessionId);
            ctx.close();
            logger.warn("illegal connection, close it.");
            return;
        }

        Resp loginRespMsg = new LoginResp(sessionId);
        if (checkRes.isPass()) {
            if (!checkRes.isOnline()) {
                Session session = new Session(sessionId, userId, token, new Date().getTime(), Session.STATUS_ONLINE);
                sessionService.add(sessionId, session);
                userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
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
                            Resp respMsg = new OfflineMsgsResp(ClientChannelIdUtil.getId(ctx), perOfflineMsgs, Const.CommonConst.SERVER_USER_ID + Const.CommonConst.KEY_SEP + new Date().getTime(), userId);
                            writeAndFlush(ctx.channel(), respMsg);

                            String msgNo = respMsg.getMsgNo();
                            respMsgService.saveUnAck(msgNo, respMsg);
                            unAckRespMsgService.add(msgNo);

                            perOfflineMsgs = new ArrayList<String>();
                            k = 0;
                        }
                        perOfflineMsgs.add(linkedOfflineMsgs.pop());
                        k ++;
                    }

                }
            } else {
                Long kSessionId = userStatusService.getSessionId(userId);
                if (kSessionId == null || kSessionId.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
                    return;
                }
                Session kickSession = sessionService.kick(kSessionId);

                if (kickSession != null) {
                    RespMsgPublisher.publish(kSessionId, new KickedResp(ClientChannelIdUtil.getId(ctx)));
                    logger.info("user(" + userId + ") is login in more than onece, kick the first login.");
                }
                Session session = new Session(sessionId, userId, token, new Date().getTime(), Session.STATUS_ONLINE);
                sessionService.add(sessionId, session);
                userStatusService.userOnline(userId, token, sessionId, new Date().getTime());
                writeAndFlush(ctx.channel(), loginRespMsg);
                logger.info("user(" + userId + ") login, return session id " + sessionId);
            }
        }
    }

}
