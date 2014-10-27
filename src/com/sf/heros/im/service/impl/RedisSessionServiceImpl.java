package com.sf.heros.im.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import redis.clients.jedis.ScanResult;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.redis.RedisCmdPair;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.SessionService;

public class RedisSessionServiceImpl implements SessionService {

    private static final Logger logger = Logger.getLogger(RedisSessionServiceImpl.class);

    private RedisManagerV2 rm;

    public RedisSessionServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    private String getKey(String id) {
        return Const.RedisConst.SESSION_KEY_PREFIX + id;
    }

    @Override
    public boolean add(String id, Session session) {
        String key = getKey(id);
        List<RedisCmdPair> cmdPairs = new ArrayList<RedisCmdPair>();
        Map<String, Object> serial = session.serialToMap();
        Set<Entry<String, Object>> serialEntries = serial.entrySet();
        for (Entry<String, Object> serialEntry : serialEntries) {
            cmdPairs.add(new RedisCmdPair("hset", new Object[]{key, serialEntry.getKey(), serialEntry.getValue().toString()}));
        }
        return rm.oMulti(cmdPairs);
    }

    @Override
    public Session get(String id) {
        String key = getKey(id);
        try {
            Map<String, String> serial = rm.hgetAll(key);
            if (serial == null || serial.isEmpty()) {
                return null;
            }
            Session session = new Session();
            session.fillFromSerial(serial);
            return session;
        } catch (RedisConnException e) {
            logger.error("get session(" + id + ") error", e);
            return null;
        }
    }

    @Override
    public void updatePingTime(String id) {
        rm.hset(getKey(id), "pingTime", new Date().getTime() + "");
    }

    @Override
    public void del(String id) {
        rm.del(getKey(id));
    }

    @Override
    public Session kick(String id) {
        rm.hset(getKey(id), "status", Session.STATUS_KICKED + "");
        return get(id);
    }

    @Override
    public void delAll() {
        try {
            String cursor = "0";
            while (true) {
                ScanResult<String> scanResult = rm.scan(cursor, getKeyPattern(), 100);
                List<String> keys = scanResult.getResult();
                if (keys != null && !keys.isEmpty()) {
                    rm.del(keys.toArray(new String[keys.size()]));
                }
                cursor = scanResult.getStringCursor();
                if (cursor.equals("0")) {
                    break;
                }
            }
        } catch (RedisConnException e) {
            throw new RuntimeException(e);
        }
    }

    private String getKeyPattern() {
        return Const.RedisConst.SESSION_KEY_PREFIX + "*";
    }
}
