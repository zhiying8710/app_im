package com.sf.heros.im.req.controller;

import io.netty.channel.ChannelHandlerContext;

import com.sf.heros.im.service.SessionService;

public class PingController extends CommonController {

    private SessionService sessionService;

    public PingController(SessionService sessionService) {
        super(sessionService);
        this.sessionService = sessionService;
    }

    @Override
    public void exec(Object msg, ChannelHandlerContext ctx, Long sessionId) {
        sessionService.updatePingTime(sessionId);

    }

}
