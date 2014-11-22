package com.sf.heros.im.server.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.thrift.ImCounterServer;

public class CounterThriftServerListener implements ChannelFutureListener {

    private static final Logger logger = Logger.getLogger(CounterThriftServerListener.class);

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

        if (future.isSuccess()) {
            try {
                new ImCounterServer().boot(PropsLoader.get(Const.PropsConst.IM_COUNTER_THRIFT_SERVER_PORT, 19000));
            } catch (Exception e) {
                logger.error("im counter server run error.", e);
            }
        }

    }

}
