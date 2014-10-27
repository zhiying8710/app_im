package com.sf.heros.im.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

import com.sf.heros.im.common.bean.msg.Msg;

public class ImUtils {

    private final static AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());

    /**
     * 生成唯一 id
     * @return
     */
    public static String getUniqueId() {
        Long uniqueMillis = lastTime.incrementAndGet();
        Long curNanos = System.nanoTime();
        String nanos = curNanos.toString();
        return uniqueMillis + nanos;
    }

    public static ByteBuf getBuf(ByteBufAllocator allocator, Msg msg) throws UnsupportedEncodingException {
        return allocator.buffer().writeBytes(msg.toJson().getBytes("utf-8"));
    }

    public static String getUniqueChannelId(String remoteHost) {
        Long uniqueMillis = lastTime.incrementAndGet();
        Long curNanos = System.nanoTime();
        String nanos = curNanos.toString();
        return MD5Util.encodeByMD5(uniqueMillis + nanos + remoteHost, remoteHost);
    }

    public static ByteBuf getBuf(ByteBufAllocator allocator, String msg) throws UnsupportedEncodingException {
        return allocator.buffer().writeBytes(msg.getBytes("utf-8"));
    }

    public static String getRespSubChannel(String sessionId) {
        return Const.RedisConst.RESP_MSG_SUB_KEY_PREFIX + sessionId;
    }

}
