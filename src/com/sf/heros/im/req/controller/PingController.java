package com.sf.heros.im.req.controller;

import com.sf.heros.im.service.SessionService;

public class PingController extends CommonController {

    private SessionService sessionService;

    public PingController(SessionService sessionService) {
        super(null, null);
        this.sessionService = sessionService;
    }

    @Override
    public void exec(Object msg, Long sessionId, boolean needAck) {
        sessionService.updatePingTime(sessionId);

    }

}
