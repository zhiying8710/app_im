package com.sf.heros.im.req.controller;

import com.sf.heros.im.channel.ClientChannelGroup;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class LogoutController extends CommonController {

    private UserStatusService userStatusService;
    private SessionService sessionService;

    public LogoutController(UserStatusService userStatusService, SessionService sessionService) {
        super(sessionService, userStatusService);
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
    }

    @Override
    public void exec(Object msg, Long sessionId, boolean needAck) throws Exception {
        Req reqMsg = transfer(msg);
        String from = sessionService.get(reqMsg.getSid()).getUserId();
        userStatusService.userOffline(from);
        sessionService.del(reqMsg.getSid());
        ClientChannelGroup.close(sessionId);
    }
}
