package com.sf.heros.im.resp.mq;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.common.RespPublisher;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.bean.msg.ChatResp;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.req.controller.CommonController;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class RespQueueConsumer extends RespEndPoint implements Runnable, Consumer {

    private static final Logger logger = Logger.getLogger(RespQueueConsumer.class);

    private static final String SERVER_ID = PropsLoader.get(Const.PropsConst.SERVER_ID);

    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;

    public RespQueueConsumer(UserStatusService userStatusService, SessionService sessionService, RespMsgService respMsgService) {
        super();
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
        this.respMsgService = respMsgService;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {

        logger.info("Consumer " + consumerTag + " registered on " + Thread.currentThread().getName() + ", now recover all un-ack msg.");
        try {
            channel.basicRecover();
        } catch (IOException e) {
        }
    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
            BasicProperties properties, byte[] body) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();
        ChatResp resp = null;
        try {
            resp = Resp.fromJson(new String(body, "utf-8"), ChatResp.class);
        } catch (Exception e) {
        }
        if (resp == null) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        String to = resp.getTo();
        Long sessionId = userStatusService.getSessionId(to);
        if (sessionId == Const.ProtocolConst.EMPTY_SESSION_ID) {
            channel.basicReject(deliveryTag, true);
            logger.error("redis error, reject msg and requeue.");
            return;
        }
        Session session = null;
        if (sessionId == null) { // logout
            respMsgService.saveOffline(to, resp);
        } else {
            try {
                sessionService.updatePingTime(sessionId);
                session = sessionService.get(sessionId);
                if (session == null) { // logout
                    respMsgService.saveOffline(to, resp);
                } else {
                    String serverId = session.getServerId();
                    if (SERVER_ID.equals(serverId)) {
                        CommonController.get(resp.getType()).exec(resp, sessionId, false);
                    } else {
                        RespPublisher.publish(sessionId, serverId, resp);
                    }
                }
            } catch (Exception e) {
                channel.basicReject(deliveryTag, true);
                logger.error("get session error, reject msg and requeue.");
                return;
            }
        }
        channel.basicAck(deliveryTag, false);
    }

    @Override
    public void handleShutdownSignal(String consumerTag,
            ShutdownSignalException sig) {

    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        logger.info("Consumer " + consumerTag + " is revovered.");
    }

    @Override
    public void run() {

        try {
            channel.basicConsume(endPointName, false, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
