package com.sf.heros.im.req.controller;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;

public class AckController extends CommonController {

    private static final Logger logger = Logger.getLogger(AckController.class);

    private SessionService sessionService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;

    public AckController(SessionService sessionService,
            RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService) {
        super(sessionService, null);
        this.sessionService = sessionService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void exec(Object msg, Long sessionId, boolean needAck) throws Exception {
        sessionService.updatePingTime(sessionId);

        Req reqMsg = transfer(msg);
        String msgNo = reqMsg.getFromData(Const.ReqAckConst.DATA_SRC_MSG_NO).toString();
        logger.info("got ack for " + msgNo);
        synchronized (msgNo) {
            unAckRespMsgService.remove(msgNo);
            respMsgService.delUnAck(msgNo);
            respMsgService.delOffline(sessionService.get(sessionId).getUserId(), msgNo);
            logger.info("remove unack resp msg for " + msgNo);
        }
    }

}
