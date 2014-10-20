package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.RespMsg;
import com.sf.heros.im.common.Session;
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
                        RespMsg respMsg = RespMsg.fromJson(unAckRespMsg, RespMsg.class);
                        String toUserId = respMsg.getFromData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, "null").toString();
                        String sessionId = ReSendUnAckRespMsgHandler.this.userStatusService.getSessionId(toUserId);
                        String unAckMsgId = ReSendUnAckRespMsgHandler.this.getUnAckMsgId(respMsg);
                        synchronized (unAckMsgId) {
                            String unAck = ReSendUnAckRespMsgHandler.this.respMsgService.getUnAck(unAckMsgId);
                            if (unAck == null) {
                                continue;
                            }
                            if (sessionId != null) {
                                Session session = ReSendUnAckRespMsgHandler.this.sessionService.get(sessionId);
                                if (session == null) {
                                    ReSendUnAckRespMsgHandler.this.userStatusService.userOffline(toUserId);
                                    ReSendUnAckRespMsgHandler.this.respMsgService.saveOffline(toUserId, respMsg);
                                } else {
                                    Channel toChannel = session.getChannel();
                                    try {
                                        toChannel.writeAndFlush(ImUtils.getBuf(toChannel.alloc(), respMsg));
                                        ReSendUnAckRespMsgHandler.this.unAckRespMsgService.add(unAckMsgId);
                                        logger.info("resend msg " + unAck + " and re-wheel.");
                                    } catch (UnsupportedEncodingException e) {
                                    }
                                }
                            } else {
                                ReSendUnAckRespMsgHandler.this.respMsgService.saveOffline(toUserId, respMsg);
                            }
                        }
                    }
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("release resource.");
                ReSendUnAckRespMsgHandler.this.release();
            }
        }));

    }

    public void release() {

        this.executor.shutdown();

    }

}
