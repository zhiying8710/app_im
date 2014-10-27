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
            String unAckMsg = rm.hget(getRespMsgUnAckKey(), unAckRespMsgId);
            rm.hdel(getRespMsgUnAckKey(), unAckRespMsgId);
            return unAckMsg;
        } catch (RedisConnException e) {
            return null;
        }

    }

    private String getRespMsgUnAckKey() {
        return Const.RedisConst.RESP_MSG_UNACK_KEY;
    }

    @Override
    public void saveUnAck(String unAckRespMsgId, RespMsg respMsg) {

        rm.hset(getRespMsgUnAckKey(), unAckRespMsgId, respMsg.toJson());

    }

    @Override
    public void saveOffline(String userId, RespMsg respMsg) {
        rm.rpush(getUserOfflineMsgKey(userId), respMsg.toJson());
    }

    private String getUserOfflineMsgKey(String userId) {
        return Const.RedisConst.USER_OFFLINE_MSG_KEY_PREFIX + userId;
    }

    @Override
    public List<String> getOfflines(String userId) {

        try {
            String key = getUserOfflineMsgKey(userId);
            List<String> offlines = rm.lAll(key);
            rm.del(key);
            return offlines;
        } catch (RedisConnException e) {
            return null;
        }

    }

    @Override
    public void delUnAck(String unAckRespMsgId) {

        rm.hdel(getRespMsgUnAckKey(), unAckRespMsgId);

    }

    @Override
    public RespMsg getUnAckMsg(String unAckRespMsgId) {
        try {
            String msg = rm.hget(getRespMsgUnAckKey(), unAckRespMsgId);
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
            List<String> msgs = rm.lAll(getUserOfflineMsgKey(userId));
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
