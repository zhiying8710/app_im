package com.sf.heros.im.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanResult;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.SessionService;

public class RedisSessionServiceImpl implements SessionService {

    private static final Logger logger = Logger.getLogger(RedisSessionServiceImpl.class);

    private RedisManagerV2 rm;

    public RedisSessionServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    private String getKey(Long id) {
        return Const.RedisConst.SESSION_KEY_PREFIX + id;
    }

    @Override
    public boolean add(Long id, Session session) {
        if (id == null || id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            return true;
        }
        String key = getKey(id);

//        Map<String, Object> serial = session.serialToMap();
//        List<RedisCmdPair> cmdPairs = new ArrayList<RedisCmdPair>();
//        for (Entry<String, Object> serialEntry : serial.entrySet()) {
//            cmdPairs.add(new RedisCmdPair("hset", new Object[]{key, serialEntry.getKey(), serialEntry.getValue().toString()}));
//        }
//        cmdPairs.add(new RedisCmdPair("expire", new Object[]{key, Const.CommonConst.SESSION_TIMEOUT_SECS}));
//        return rm.pipeline(cmdPairs);

//        Pipeline pipeline = rm.pipeline();
//        if (pipeline == null) {
//            throw new NullPointerException("can not get redis pipeline.");
//        }
//        Map<String, Object> serial = session.serialToMap();
//        for (Entry<String, Object> serialEntry : serial.entrySet()) {
//            pipeline.hset(key, serialEntry.getKey(), serialEntry.getValue().toString());
//        }
//        pipeline.expire(key, Const.CommonConst.SESSION_TIMEOUT_SECS);
//        pipeline.multi();
//        try {
//            pipeline.exec();
//        } catch (Exception e) {
//            logger.error("pipeline exec error.", e);
//            pipeline.discard();
//        }
//        return true;

        class AddSessionDeal implements DealWithSession {

            private String key;
            private Session session;

            public AddSessionDeal(String key, Session session) {
                super();
                this.key = key;
                this.session = session;
            }

            @Override
            public void deal(Pipeline pipeline) {
                Map<String, Object> serial = session.serialToMap();
                for (Entry<String, Object> serialEntry : serial.entrySet()) {
                    pipeline.hset(key, serialEntry.getKey(), serialEntry.getValue().toString());
                }
                pipeline.expire(key, Const.CommonConst.SESSION_TIMEOUT_SECS);
            }

        }
        return dealWithSession(new AddSessionDeal(key, session));

    }

    @Override
    public Session get(Long id) {
        if (id == null || id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            return null;
        }
        String key = getKey(id);
        boolean ex = true;
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
            ex = false;
            return null;
        } finally {
            if (ex) {
                try {
                    rm.expire(key, Const.CommonConst.SESSION_TIMEOUT_SECS);
                } catch (RedisConnException e) {
                }
            }
        }
    }

    @Override
    public void updatePingTime(Long id) {
        if (id == null || id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID) {
            return;
        }
        String key = getKey(id);

//        List<RedisCmdPair> cmdPairs = new ArrayList<RedisCmdPair>();
//        cmdPairs.add(new RedisCmdPair("hset", new Object[]{key, "pingTime", new Date().getTime() + ""}));
//        cmdPairs.add(new RedisCmdPair("expire", new Object[]{key, Const.CommonConst.SESSION_TIMEOUT_SECS}));
//        rm.pipeline(cmdPairs);



//        Pipeline pipeline = rm.pipeline();
//        if (pipeline == null) {
//            throw new NullPointerException("can not get redis pipeline.");
//        }
//        pipeline.multi();
//        pipeline.hset(key, "pingTime", new Date().getTime() + "");
//        pipeline.expire(key, Const.CommonConst.SESSION_TIMEOUT_SECS);
//        try {
//            pipeline.exec();
//        } catch (Exception e) {
//            logger.error("pipeline exec error.", e);
//            pipeline.discard();
//        }

        class UpdatePingTimeDeal implements DealWithSession {

            private String key;

            public UpdatePingTimeDeal(String key) {
                super();
                this.key = key;
            }

            @Override
            public void deal(Pipeline pipeline) {
                pipeline.hset(key, "pingTime", new Date().getTime() + "");
                pipeline.expire(key, Const.CommonConst.SESSION_TIMEOUT_SECS);
            }

        }

        dealWithSession(new UpdatePingTimeDeal(key));
    }

    interface DealWithSession {

        public void deal(Pipeline pipeline);

    }

    private boolean dealWithSession(DealWithSession dealWithSession) {
        Jedis j = null;
        Pipeline pipeline = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = rm.connect();
            pipeline = j.pipelined();
            pipeline.multi();
            dealWithSession.deal(pipeline);
            pipeline.exec();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (pipeline != null) {
                pipeline.discard();
            }
            borrowOrOprSuccess = false;
            rm.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                rm.disConnected(j);
            }
        }
    }

    @Override
    public void del(Long id) {
        if (id == null || id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            return;
        }
        rm.del(getKey(id));
    }

    @Override
    public Session kick(Long id) {
        if (id == null || id.longValue() == Const.ProtocolConst.EMPTY_SESSION_ID.longValue()) {
            return null;
        }
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
