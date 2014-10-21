package com.sf.heros.im.service.impl;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.UnAckRespMsgQueue;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.timingwheel.UnAckRespMsgFixIntervalTimingWheel;

public class MemUnAckRespMsgServiceImpl implements
        UnAckRespMsgService {

    private static final Logger logger = Logger.getLogger(MemUnAckRespMsgServiceImpl.class);

    private UnAckRespMsgFixIntervalTimingWheel unAckRespMsgTimingWheel;

    public MemUnAckRespMsgServiceImpl(UnAckRespMsgFixIntervalTimingWheel unAckRespMsgTimingWheel) {
        this.unAckRespMsgTimingWheel = unAckRespMsgTimingWheel;
        this.unAckRespMsgTimingWheel.setUnAckRespMsgService(this);
        this.unAckRespMsgTimingWheel.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("release resource.");
                MemUnAckRespMsgServiceImpl.this.release();
            }
        }));
    }

    @Override
    public String popFromQueue() {
        return UnAckRespMsgQueue.getFirst();
    }

    @Override
    public boolean pushToQueue(String unAckMsg) {
        return UnAckRespMsgQueue.addLast(unAckMsg);
    }

    @Override
    public boolean pushToQueue(RespMsg unAckMsg) {
        return UnAckRespMsgQueue.addLast(unAckMsg.toJson());
    }

    @Override
    public void add(String unAckMsgId) {
        unAckRespMsgTimingWheel.add(unAckMsgId);
    }

    @Override
    public void remove(String unAckMsgId) {
        unAckRespMsgTimingWheel.remove(unAckMsgId);
    }

    @Override
    public void release() {
        this.unAckRespMsgTimingWheel.stop();
    }

}
