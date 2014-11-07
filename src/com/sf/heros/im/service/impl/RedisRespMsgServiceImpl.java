package com.sf.heros.im.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.common.redis.RedisCmdPair;
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
    public void saveUnAck(String unAckRespMsgId, Resp respMsg) {

        rm.hset(getRespMsgUnAckKey(), unAckRespMsgId, respMsg.toJson());

    }

    @Override
    public void saveOffline(String userId, Resp respMsg) {
    	rm.hset(getUserOfflineMsgKey(userId), respMsg.getMsgNo(), respMsg.toJson());
//        rm.rpush(getUserOfflineMsgKey(userId), respMsg.toJson());
    }

    private String getUserOfflineMsgKey(String userId) {
        return Const.RedisConst.USER_OFFLINE_MSG_KEY_PREFIX + userId;
    }

    @Override
    public List<String> getOfflines(String userId) {

        try {
            String key = getUserOfflineMsgKey(userId);
            List<String> offlines = rm.hvals(key);
//            List<String> offlines = rm.lAll(key);
            rm.del(key);
            return offlines;
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void delUnAck(String unAckRespMsgId) {

        rm.hdel(getRespMsgUnAckKey(), unAckRespMsgId);

    }

    @Override
    public Resp getUnAckMsg(String unAckRespMsgId) {
        try {
            String msg = rm.hget(getRespMsgUnAckKey(), unAckRespMsgId);
            if (StringUtils.isBlank(msg)) {
                return null;
            }
            return Resp.fromJson(msg, Resp.class);
        } catch (RedisConnException e) {
            return null;
        }
    }

    @Override
    public List<Resp> getOfflineMsgs(String userId) {

        try {
        	String key = getUserOfflineMsgKey(userId);
			List<String> msgs = rm.hvals(key);
//            List<String> msgs = rm.lAll(key);
            if (msgs == null || msgs.isEmpty()) {
                return null;
            }
            List<Resp> offlines = new ArrayList<Resp>();
            for (String msg : msgs) {
                offlines.add(Resp.fromJson(msg, Resp.class));
            }
            rm.del(key);
            return offlines;
        } catch (Exception e) {
            return null;
        }

    }

	@Override
	public void delOffline(String userId, String msgNo) {
		List<RedisCmdPair> cmdPairs = new ArrayList<RedisCmdPair>();
		cmdPairs.add(new RedisCmdPair("hdel", new Object[]{getRespMsgUnAckKey(), msgNo}));
		cmdPairs.add(new RedisCmdPair("hdel", new Object[]{Const.RedisConst.UNACKMSG_RESEND_COUNT_KEY, msgNo}));
		rm.oMulti(cmdPairs);
	}

}

