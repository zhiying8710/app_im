package com.sf.heros.im.server.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.common.RespPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.server.ShutdownHookUtils;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserStatusService;

public class ReSendUnAckRespLinstener implements ChannelFutureListener {

    private static final Logger logger = Logger.getLogger(ReSendUnAckRespLinstener.class);

    private static final int UNACKMSG_MAX_COUNT = PropsLoader.get(Const.PropsConst.UNACKMSG_RESEND_COUNT, 2);

    private final RedisManagerV2 rm = RedisManagerV2.getInstance();

     private SessionService sessionService;
     private UserStatusService userStatusService;
     private RespMsgService respMsgService;
     private UnAckRespMsgService unAckRespMsgService;
     private ExecutorService executor;
     private int poolSize;

    public ReSendUnAckRespLinstener(SessionService sessionService,
            UserStatusService userStatusService, RespMsgService respMsgService, UnAckRespMsgService unAckRespMsgService, int poolSize) {
        this.sessionService = sessionService;
        this.userStatusService = userStatusService;
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
        this.poolSize = poolSize;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {

            executor = Executors.newFixedThreadPool(poolSize);
            logger.info("start a exector for resend unack resp msg, poll size : " + poolSize + ".");
            for (int i = 0; i < poolSize; i++) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(0);
                            } catch (InterruptedException e) {
                            }
                            String unAckRespMsg = unAckRespMsgService.popFromQueue();
                            if (unAckRespMsg == null) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(500);
                                    continue;
                                } catch (InterruptedException e) {
                                }
                            }
                            Resp respMsg = Resp.fromJson(unAckRespMsg, Resp.class);
                            String toUserId = respMsg.getFromData(Const.RespConst.DATA_KEY_TO_USER_ID, "null").toString();
                            Long sessionId = userStatusService.getSessionId(toUserId);
                            String msgNo = respMsg.getMsgNo();
                            synchronized (msgNo) {
                                try {
                                    if (rm.hincrby(Const.RedisConst.UNACKMSG_RESEND_COUNT_KEY, msgNo, 0) > UNACKMSG_MAX_COUNT) {
                                        logger.error("msg " + msgNo + " to user " + toUserId + " has sent more than " + UNACKMSG_MAX_COUNT + "time, so just discrad it.");
                                        respMsgService.delOffline(toUserId, msgNo);
                                        continue;
                                    }
                                } catch (RedisConnException e) {
                                }
                                String unAck = respMsgService.getUnAck(msgNo);
                                if (unAck == null) {
                                    continue;
                                }
                                if (sessionId == null || sessionId.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
                                    respMsgService.saveOffline(toUserId, respMsg);
                                    continue;
                                }
                                Session session = null;
                                try {
                                    session = sessionService.get(sessionId);
                                } catch (Exception e) {
                                    unAckRespMsgService.add(msgNo);
                                    logger.error("get session by id error, re-wheel msg.");
                                    return;
                                }
                                if (session == null) {
                                    userStatusService.userOffline(toUserId);
                                    respMsgService.saveOffline(toUserId, respMsg);
                                    continue;
                                }

                                boolean incr = true;
                                try {
                                    RespPublisher.publish(sessionId, session.getServerId(), respMsg);
                                } catch (Exception e) {
                                    logger.error("publish msg error.", e);
                                    incr = false;
                                }
                                unAckRespMsgService.add(msgNo);
                                if (incr) {
                                    try {
                                        rm.hincrby(Const.RedisConst.UNACKMSG_RESEND_COUNT_KEY, msgNo, 1);
                                    } catch (RedisConnException e) {
                                    }
                                }
                                logger.info("resend msg " + unAck + " and re-wheel.");
                            }
                        }
                    }
                });
            }

            ShutdownHookUtils.shutdownExecutor(executor);
        }
    }
}
