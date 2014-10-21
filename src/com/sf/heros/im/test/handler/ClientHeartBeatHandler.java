package com.sf.heros.im.test.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.ReqMsg;

public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ClientHeartBeatHandler.class);

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        final ReqMsg msg = new ReqMsg();
//        Map<String, Object> data = new HashMap<String, Object>();
//        data.put("content", "hahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahahhahahahahahhahahahhahahah");
//        final HerosMsg msg = new HerosMsg(Const.HEROS_MSG_TYPE_PING, data, new Date().getTime());
        final Timer heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (!ctx.isRemoved() && ctx.channel().isActive()) {
                        ctx.channel().writeAndFlush(ctx.alloc().directBuffer().writeBytes(msg.toJson().getBytes("utf-8")));
                    } else {
                        logger.warn("channel is inactive, shutdown client.");
                        heartbeatTimer.cancel();
                        if (ctx != null) {
                            logger.warn("close channel");
                            ctx.channel().closeFuture().syncUninterruptibly();
                            ctx.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.out.println(new Date().getTime());
            }
        }, 3000, Const.CommonConst.HEART_BEAT_INTERVAL_SECS * 1000);
        logger.info("client channel active, new thread send heartbeat.");
        super.channelActive(ctx);
    }

}
