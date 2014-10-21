package com.sf.heros.im.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.RespMsgService;

public class RedisRespMsgServiceImpl implements RespMsgService {

    private RedisManagerV2 rm;

    public RedisRespMsgServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    @Override
    public String getUnAck(String unAckRespMsgId) {

        try {
            return rm.hget(true, Const.RedisKeyValConst.RESP_MSG_UNACK_KEY, unAckRespMsgId);
        } catch (RedisConnException e) {
            return null;
        }

    }

    @Override
    public void saveUnAck(String unAckRespMsgId, RespMsg respMsg) {

        rm.hset(true, Const.RedisKeyValConst.RESP_MSG_UNACK_KEY, unAckRespMsgId, respMsg.toJson());

    }

    @Override
    public void saveOffline(String userId, RespMsg respMsg) {
        rm.rpush(true, Const.RedisKeyValConst.USER_OFFLINE_MSG_KEY_PREFIX + userId, respMsg.toJson());
    }

    @Override
    public List<String> getOfflines(String userId) {

        try {
            return rm.lAll(true, Const.RedisKeyValConst.USER_OFFLINE_MSG_KEY_PREFIX + userId);
        } catch (RedisConnException e) {
            return null;
        }

    }

    @Override
    public void delUnAck(String unAckRespMsgId) {

        rm.hdel(true, Const.RedisKeyValConst.RESP_MSG_UNACK_KEY, unAckRespMsgId);

    }

    @Override
    public RespMsg getUnAckMsg(String unAckRespMsgId) {
        try {
            String msg = rm.hget(true, Const.RedisKeyValConst.RESP_MSG_UNACK_KEY, unAckRespMsgId);
            if (StringUtils.isBlank(msg)) {
                return null;
            }
            return RespMsg.fromJson(msg, RespMsg.class);
        } catch (RedisConnException e) {
            return null;
        }
    }

    @Override
    public List<RespMsg> getOfflineMsgs(String userId) {

        try {
            List<String> msgs = rm.lAll(true, Const.RedisKeyValConst.USER_OFFLINE_MSG_KEY_PREFIX + userId);
            if (msgs == null || msgs.isEmpty()) {
                return null;
            }
            List<RespMsg> offlines = new ArrayList<RespMsg>();
            for (String msg : msgs) {
                offlines.add(RespMsg.fromJson(msg, RespMsg.class));
            }
            return offlines;
        } catch (RedisConnException e) {
            return null;
        }

    }

}
