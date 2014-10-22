package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import redis.clients.jedis.JedisPubSub;

import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.redis.RedisManagerV2;

public class RespMsgSubHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(RespMsgSubHandler.class);

    private ExecutorService subExecutor = Executors.newFixedThreadPool(1);

    private class RespMsgSubTask implements Callable<Void> {

        private String sessionId;
        private JedisPubSub respMsgSub;
        private final RedisManagerV2 rm = RedisManagerV2.getInstance();

        public RespMsgSubTask(String sessionId, JedisPubSub respMsgSub) {
            super();
            this.sessionId = sessionId;
            this.respMsgSub = respMsgSub;
        }

        @Override
        public Void call() throws Exception {
            rm.subscribe(respMsgSub, ImUtils.getRespSubChannel(sessionId));
            return null;
        }

    }

    private class RespMsgSub extends JedisPubSub {

        private Channel soChannel;

        public RespMsgSub(Channel soChannel) {
            this.soChannel = soChannel;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                soChannel.writeAndFlush(ImUtils.getBuf(soChannel.alloc(), message));
                logger.info("got message(" + message + ") from redis channel " + channel + " and write out.");
            } catch (UnsupportedEncodingException e) {
            }
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {

        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            logger.info("sub on " + channel);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            logger.info("unsub on " + channel);
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {

        }

    }

    private RespMsgSub respMsgSub;

    private boolean inited = false;
    private boolean released = false;
    private Lock lock = new ReentrantReadWriteLock().writeLock();

    private void init(ChannelHandlerContext ctx) {
        if (!inited) {
            lock.lock();
            try {
                if (!inited) {
                    inited = true;
                    respMsgSub = new RespMsgSub(ctx.channel());
                    subExecutor.submit(new RespMsgSubTask(getSessionId(ctx), respMsgSub));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void release() {

        if (!released) {
            lock.lock();
            try {
                if (!released) {
                    released = true;
                    respMsgSub.unsubscribe();
                    subExecutor.shutdownNow();
                }
            } finally {
                lock.unlock();
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        init(ctx);
        super.channelActive(ctx);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        init(ctx);
        super.channelRegistered(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        release();
        super.channelInactive(ctx);

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

        release();
        super.channelUnregistered(ctx);

    }


}
