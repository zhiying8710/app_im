package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.RespMsgPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserStatusService;

@Sharable
public class ReSendUnAckRespMsgHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(ReSendUnAckRespMsgHandler.class);

    private SessionService sessionService;
    private UserStatusService userStatusService;
    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;
    private ExecutorService executor;

    public ReSendUnAckRespMsgHandler(SessionService sessionService,
            UserStatusService userStatusService, RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService, int poolSize) {
        super();
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;

        executor = Executors.newFixedThreadPool(poolSize);
        logger.info("start a exector for resend unack resp msg, poll size : " + poolSize + ".");
        for (int i = 0; i < poolSize; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        String unAckRespMsg = ReSendUnAckRespMsgHandler.this.unAckRespMsgService.popFromQueue();
                        if (unAckRespMsg == null) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                                continue;
                            } catch (InterruptedException e) {
                            }
                        }
                        Resp respMsg = Resp.fromJson(unAckRespMsg, Resp.class);
                        String toUserId = respMsg.getFromData(Const.RespConst.DATA_KEY_TO_USER_ID, "null").toString();
                        Long sessionId = ReSendUnAckRespMsgHandler.this.userStatusService.getSessionId(toUserId);
                        String msgNo = respMsg.getMsgNo();
                        synchronized (msgNo) {
                            String unAck = ReSendUnAckRespMsgHandler.this.respMsgService.getUnAck(msgNo);
                            if (unAck == null) {
                                continue;
                            }
                            if (sessionId != null && sessionId.longValue() != Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
                                Session session = ReSendUnAckRespMsgHandler.this.sessionService.get(sessionId);
                                if (session == null) {
                                    ReSendUnAckRespMsgHandler.this.userStatusService.userOffline(toUserId);
                                    ReSendUnAckRespMsgHandler.this.respMsgService.saveOffline(toUserId, respMsg);
                                } else {
                                    if (RespMsgPublisher.publish(sessionId, respMsg)) {
                                        ReSendUnAckRespMsgHandler.this.unAckRespMsgService.add(msgNo);
                                        logger.info("resend msg " + unAck + " and re-wheel.");
                                    } else {
                                        ReSendUnAckRespMsgHandler.this.respMsgService.saveOffline(toUserId, respMsg);
                                    }
                                }
                            } else {
                                ReSendUnAckRespMsgHandler.this.respMsgService.saveOffline(toUserId, respMsg);
                            }
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(0);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                ReSendUnAckRespMsgHandler.this.release();
                logger.info("release resource.");
            }
        }));

    }

    public void release() {

        this.executor.shutdown();

    }

}
