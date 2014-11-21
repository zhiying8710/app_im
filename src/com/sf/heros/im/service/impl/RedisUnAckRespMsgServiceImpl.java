package com.sf.heros.im.service.impl;

import org.apache.log4j.Logger;

import com.sf.heros.im.AppMain;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.timingwheel.UnAckRespMsgFixIntervalTimingWheel;

public class RedisUnAckRespMsgServiceImpl implements
        UnAckRespMsgService {

    private static final Logger logger = Logger.getLogger(RedisUnAckRespMsgServiceImpl.class);

    private UnAckRespMsgFixIntervalTimingWheel unAckRespMsgTimingWheel;
    private RedisManagerV2 rm;

    public RedisUnAckRespMsgServiceImpl(UnAckRespMsgFixIntervalTimingWheel unAckRespMsgTimingWheel) {
        this.unAckRespMsgTimingWheel = unAckRespMsgTimingWheel;
        this.unAckRespMsgTimingWheel.setUnAckRespMsgService(this);
        this.unAckRespMsgTimingWheel.start();
        this.rm = RedisManagerV2.getInstance();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("release resource.");
                RedisUnAckRespMsgServiceImpl.this.release();
            }
        }));
    }

    private String unAckRespMsgResendKey() {
        return Const.RedisConst.RESP_MSG_UNACK_RESEND_KEY + Const.CommonConst.KEY_SEP + AppMain.SERVER_ID;
    }

    @Override
    public String popFromQueue() {
        try {
            return rm.lpop(unAckRespMsgResendKey());
        } catch (RedisConnException e) {
            return null;
        }
    }

    @Override
    public boolean pushToQueue(String unAckMsg) {
        return rm.rpush(unAckRespMsgResendKey(), unAckMsg);
    }

    @Override
    public boolean pushToQueue(Resp unAckMsg) {
        return pushToQueue(unAckMsg.toJson());
    }

    @Override
    public void add(String msgNo) {
        unAckRespMsgTimingWheel.add(msgNo);
    }

    @Override
    public void remove(String msgNo) {
        unAckRespMsgTimingWheel.remove(msgNo);
    }

    @Override
    public void release() {
        this.unAckRespMsgTimingWheel.stop();
    }

}
