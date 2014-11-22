package com.sf.heros.im.server.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.resp.mq.RespQueueConsumer;
import com.sf.heros.im.server.ShutdownHookUtils;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class RespMqListener implements ChannelFutureListener {

    private static final Logger logger = Logger.getLogger(RespMqListener.class);

    private UserStatusService userStatusService;
    private SessionService sessionService;
    private RespMsgService respMsgService;

    public RespMqListener(UserStatusService userStatusService,
            SessionService sessionService, RespMsgService respMsgService) {
        this.userStatusService = userStatusService;
        this.sessionService = sessionService;
        this.respMsgService = respMsgService;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            Integer consumerThs = PropsLoader.get(Const.PropsConst.RESP_MQ_CONSUMER_THS, 5);
            ExecutorService executor = Executors.newFixedThreadPool(consumerThs);
            logger.info("init " + consumerThs + " threads executor for resp mq consumer.");
            for (int i = 0; i < consumerThs; i++) {
                executor.submit(new RespQueueConsumer(userStatusService, sessionService, respMsgService));
            }
            ShutdownHookUtils.shutdownExecutor(executor);
        }
    }

}
