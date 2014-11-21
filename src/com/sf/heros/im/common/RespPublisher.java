package com.sf.heros.im.common;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sf.heros.im.AppMain;
import com.sf.heros.im.channel.ClientChannel;
import com.sf.heros.im.channel.ClientChannelGroup;
import com.sf.heros.im.channel.listener.WriteAndFlushFailureListener;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.resp.mq.RespProducer;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class RespPublisher {

    private static SessionService sessionServ;
    private static UserStatusService userStatusServ;
    private static AtomicBoolean inited = new AtomicBoolean(false);

    private static final RespProducer RESP_PRODUCER = new RespProducer();

    public static void init(SessionService sessionService, UserStatusService userStatusService) {
        sessionServ = sessionService;
        userStatusServ = userStatusService;
        inited.set(true);
    }

    public static void publish(Long sessionId, String serverId, Resp msg) throws Exception {
        if (!inited.get()) {
            throw new ExceptionInInitializerError("RespPublisher has not been inited yet.");
        }

        ClientChannel channel = null;
        if (AppMain.SERVER_UNIQUE_ID.equals(serverId)) {
            channel = ClientChannelGroup.get(sessionId);
        }
        if (channel == null) {
            RESP_PRODUCER.sendMessage(msg, serverId);
            return;
        }
        channel.writeAndFlush(msg).addListener(new WriteAndFlushFailureListener(sessionServ, userStatusServ));
    }

}
