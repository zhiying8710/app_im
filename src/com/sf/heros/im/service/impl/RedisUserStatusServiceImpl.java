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
    public boolean userOnline(String userId, String token, String sessionId,
            long loginTime) {
        Map<String, String> hash = new HashMap<String, String>();
        hash.put(Const.RedisKeyValConst.USER_STATUS_KEY_TOKEN, token);
        hash.put(Const.RedisKeyValConst.USER_STATUS_KEY_SO_SESSION_ID, sessionId);
        hash.put(Const.RedisKeyValConst.USER_STATUS_KEY_SO_LOGIN_TIME, loginTime + "");
        hash.put(Const.RedisKeyValConst.USER_STATUS_KEY_SO_ONLINE, Const.RedisKeyValConst.USER_STATUS_VAL_SO_ONLINE_ONLINE);
        boolean r = rm.hmset(getKey(userId), hash);
        if (r) {
            Counter.incrOnlinesAndGet();
        }
        return r;
    }

    private String getKey(String userId) {
        return Const.RedisKeyValConst.USER_STATUS_KEY_PRIFIX + userId;
    }

    @Override
    public boolean userOnline(String userId, String token, String sessionId) {
        return userOnline(userId, token, sessionId, new Date().getTime());
    }

    @Override
    public void userOffline(String userId) {
        if (rm.hset(getKey(userId), Const.RedisKeyValConst.USER_STATUS_KEY_SO_ONLINE, Const.RedisKeyValConst.USER_STATUS_VAL_SO_ONLINE_OFFLINE)) {
            Counter.decrsOnlinesAndGet();
        }
    }

    @Override
    public String getSessionId(String userId) {
        try {
            return rm.hget(getKey(userId), Const.RedisKeyValConst.USER_STATUS_KEY_SO_SESSION_ID);
        } catch (RedisConnException e) {
            return Const.RedisKeyValConst.SINGEL_ERR_VAL;
        }
    }

    @Override
    public boolean isOnline(String userId) {
        try {
            return Const.RedisKeyValConst.USER_STATUS_VAL_SO_ONLINE_ONLINE.equals(rm.hget(getKey(userId), Const.RedisKeyValConst.USER_STATUS_KEY_SO_ONLINE));
        } catch (RedisConnException e) {
            return true;
        }
    }

    @Override
    public void offlineAll() {
        try {
            Set<String> keys = rm.keys(Const.RedisKeyValConst.USER_STATUS_KEY_PRIFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            if (!rm.del(keys.toArray(new String[keys.size()]))) {
                throw new RedisConnException("del " + StringUtils.join(keys, ",") + " err.");
            }
            Counter.initOnlines();
        } catch (RedisConnException e) {
            throw new RuntimeException(e);
        }
    }


}
