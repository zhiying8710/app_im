package com.sf.heros.im.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

import com.sf.heros.im.common.bean.msg.ReqResp;

public class ImUtils {

    private final static AtomicLong lastTime = new AtomicLong(0);

    public static ByteBuf getBuf(ByteBufAllocator allocator, ReqResp msg) throws UnsupportedEncodingException {
        return allocator.buffer().writeBytes(msg.toJson().getBytes("utf-8"));
    }

    public static String getUniqueId(String slat) {
        return MD5Util.encodeByMD5(lastTime.incrementAndGet() + slat, slat);
    }

    public static ByteBuf getBuf(ByteBufAllocator allocator, String msg) throws UnsupportedEncodingException {
        return allocator.buffer().writeBytes(msg.getBytes("utf-8"));
    }

    public static String getRespSubChannel(Long sessionId) {
        return Const.RedisConst.RESP_MSG_SUB_KEY_PREFIX + sessionId;
    }
}
