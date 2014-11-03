package com.sf.heros.im.common;

import com.sf.heros.im.channel.ClientChannel;
import com.sf.heros.im.channel.ClientChannelGroup;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;

public class RespMsgPublisher {

    public static boolean publish(Long sessionId, String msg) {
        ClientChannel channel = ClientChannelGroup.get(sessionId);
        if (channel == null) {
            return false;
        }
        channel.writeAndFlush(msg);
        return true;
    }

    public static boolean publish(Long sessionId, Resp msg) {
        ClientChannel channel = ClientChannelGroup.get(sessionId);
        if (channel == null) {
            return false;
        }
        channel.writeAndFlush(msg);
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
