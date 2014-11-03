package com.sf.heros.im.req.controller;

import io.netty.channel.ChannelHandlerContext;

import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class LogoutController extends CommonController {

    private UserStatusService userStatusService;
    private SessionService sessionService;

    public LogoutController(UserStatusService userStatusService, SessionService sessionService) {
        super(sessionService);
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
    }

    @Override
    public void exec(Object msg, ChannelHandlerContext ctx, Long sessionId) {
        Req reqMsg = transfer(msg);
        String from = sessionService.get(reqMsg.getSid()).getUserId();
        userStatusService.userOffline(from);
        sessionService.del(reqMsg.getSid());
        ctx.close();
    }
}
