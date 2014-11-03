package com.sf.heros.im.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.Counter;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.UserStatusService;

public class RedisUserStatusServiceImpl implements UserStatusService {

    private RedisManagerV2 rm;

    public RedisUserStatusServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    @Override
    public boolean userOnline(String userId, String token, Long sessionId,
            long loginTime) {
        Map<String, String> hash = new HashMap<String, String>();
        hash.put(Const.RedisConst.USER_STATUS_KEY_TOKEN, token);
        hash.put(Const.RedisConst.USER_STATUS_KEY_SO_SESSION_ID, sessionId.toString());
        hash.put(Const.RedisConst.USER_STATUS_KEY_SO_LOGIN_TIME, loginTime + "");
        hash.put(Const.RedisConst.USER_STATUS_KEY_SO_ONLINE, Const.RedisConst.USER_STATUS_VAL_SO_ONLINE_ONLINE);
        boolean r = rm.hmset(getKey(userId), hash);
        if (r) {
            Counter.OnlinesCounter.INSTANCE.incrAndGet();
        }
        return r;
    }

    private String getKey(String userId) {
        return Const.RedisConst.USER_STATUS_KEY_PRIFIX + userId;
    }

    @Override
    public boolean userOnline(String userId, String token, Long sessionId) {
        return userOnline(userId, token, sessionId, new Date().getTime());
    }

    @Override
    public void userOffline(String userId) {
        try {
            if (rm.exist(getKey(userId)) && rm.hset(getKey(userId), Const.RedisConst.USER_STATUS_KEY_SO_ONLINE, Const.RedisConst.USER_STATUS_VAL_SO_ONLINE_OFFLINE)) {
                Counter.OnlinesCounter.INSTANCE.decrAndGet();
            }
        } catch (RedisConnException e) {
        }
    }

    @Override
    public Long getSessionId(String userId) {
        try {
            String sid = rm.hget(getKey(userId), Const.RedisConst.USER_STATUS_KEY_SO_SESSION_ID);
            if (sid == null) {
                return null;
            }
            return new Long(sid);
        } catch (RedisConnException e) {
            return Const.ProtocolConst.EMPTY_SESSION_ID;
        }
    }

    @Override
    public boolean isOnline(String userId) {
        try {
            return Const.RedisConst.USER_STATUS_VAL_SO_ONLINE_ONLINE.equals(rm.hget(getKey(userId), Const.RedisConst.USER_STATUS_KEY_SO_ONLINE));
        } catch (RedisConnException e) {
            return true;
        }
    }

    @Override
    public void offlineAll() {
        try {
            Set<String> keys = rm.keys(Const.RedisConst.USER_STATUS_KEY_PRIFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            if (!rm.del(keys.toArray(new String[keys.size()]))) {
                throw new RedisConnException("del " + StringUtils.join(keys, ",") + " err.");
            }
            Counter.OnlinesCounter.INSTANCE.init();
        } catch (RedisConnException e) {
            throw new RuntimeException(e);
        }
    }


}
