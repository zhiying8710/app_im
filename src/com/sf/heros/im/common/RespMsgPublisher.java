package com.sf.heros.im.common;

import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.common.redis.RedisManagerV2;

public class RespMsgPublisher {

    private static final RedisManagerV2 rm = RedisManagerV2.getInstance();

    public static void publish(String sessionId, String msg) throws Exception {

        rm.publish(ImUtils.getRespSubChannel(sessionId), msg);
    }

    public static void publish(String sessionId, RespMsg respMsg) throws Exception {
        publish(sessionId, respMsg.toJson());
    }

}
