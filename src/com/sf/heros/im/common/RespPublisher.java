package com.sf.heros.im.common;

import com.sf.heros.im.channel.ClientChannel;
import com.sf.heros.im.channel.ClientChannelGroup;
import com.sf.heros.im.channel.listener.WriteAndFlushFailureListener;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class RespPublisher {

	private static SessionService sessionServ;
	private static UserStatusService userStatusServ;

	public static void init(SessionService sessionService, UserStatusService userStatusService) {
		sessionServ = sessionService;
		userStatusServ = userStatusService;
	}

    public static boolean publish(Long sessionId, Object msg) {
        ClientChannel channel = ClientChannelGroup.get(sessionId);
        if (channel == null) {
            return false;
        }
        channel.writeAndFlush(msg).addListener(new WriteAndFlushFailureListener(sessionServ, userStatusServ));
        return true;
    }

    private static final RedisManagerV2 rm = RedisManagerV2.getInstance();

    @Deprecated
    public static boolean publishToRedis(Long sessionId, String msg) {
        try {
            rm.publish(ImUtils.getRespSubChannel(sessionId), msg);
            return true;
        } catch (RedisConnException e) {
            return false;
        }
    }

    @Deprecated
    public static boolean publishToRedis(Long sessionId, Resp msg) {
        return publish(sessionId, msg.toJson());
    }

}
