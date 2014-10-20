package com.sf.heros.im.common.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.reflect.MethodUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import redis.clients.util.Pool;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;

public class RedisManagerV2 {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static RedisManagerV2 instance = null;

    private static Pool<Jedis> pool;

    public static Long defaultDb;

    private RedisManagerV2() {
        String redisHost = PropsLoader.get(Const.PropsConst.REDIS_HOST, "127.0.0.1");
        int redisPort = PropsLoader.get(Const.PropsConst.REDIS_PORT, 6379);
        int defaultConns = PropsLoader.get(Const.PropsConst.REDIS_DF_CONNS, 100);
        defaultDb = new Long(PropsLoader.get(Const.PropsConst.REDIS_DF_DB, 0));
        int redisTimeout = PropsLoader.get(Const.PropsConst.REDIS_TIMEOUT, 10);

        JedisPoolConfig dfConfig = new JedisPoolConfig();
        dfConfig.setMaxTotal(defaultConns);
        dfConfig.setMaxIdle(defaultConns / 10); // 最大空闲数, 0为无限制
        dfConfig.setMaxWaitMillis(10 * 1000);
        dfConfig.setTestOnBorrow(true);
        dfConfig.setTestOnReturn(true);

        pool = new JedisPool(dfConfig, redisHost, redisPort, redisTimeout, null, defaultDb.intValue());

    }

    public static RedisManagerV2 getInstance() {
        if (instance == null) {
            synchronized (RedisManagerV2.class) {
                if (instance == null) {
                    instance = new RedisManagerV2();
                }
            }
        }
        return instance;
    }

    public Jedis connect(Long db) {
        try {
            lock.writeLock().lock();
            return pool.getResource();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void disConnected(Jedis j) {
        if (j != null) {
            pool.returnResource(j);
            j = null;
        }
    }

    public void returnBrokenResource(Jedis j) {
        if (j != null) {
            pool.returnBrokenResource(j);
            j = null;
        }
    }

    public Long getKeyDB(boolean df, String key) {
        return defaultDb;
    }

    /**
     * 删除一个key
     *
     * @param df
     *            是否操作的是默认数据库
     * @param keys
     * @return
     * @throws RedisConnException
     */
    public boolean del(boolean df, String key) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(getKeyDB(df, key));
            j.del(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean del(Long keyDB, String...keys) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(keyDB);
            j.del(keys);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 设置一个string类型的key的值
     *
     * @param df
     *            是否操作的是默认数据库
     * @param key
     * @param val
     * @return
     */
    public boolean set(boolean df, String key, String val) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(getKeyDB(df, key));
            j.set(key, val);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean setnx(boolean df, String key, String val) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(getKeyDB(df, key));
            return j.setnx(key, val) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 设置一个string类型的key的值
     *
     * @param db
     *            操作的数据库
     * @param key
     * @param val
     * @return
     */
    public boolean set(int db, String key, String val) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(new Long(db));
            j.set(key, val);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 获取一个string类型的key的值
     *
     * @param key
     * @return
     * @throws RedisConnException
     */
    public String get(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: get " + key
                    + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 放入一个或多个值到list的左端.
     *
     * @param df
     *            是否操作的是默认数据库
     * @param key
     * @param vals
     * @return
     */
    public boolean lpush(boolean df, String key, String... vals) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.lpush(key, vals);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 从一个list的最左边出队一个值
     * @param df
     * @param key
     * @return
     * @throws RedisConnException
     */
    public String lpop(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.lpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: lpop " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 放入一个或多个值到list的右端
     *
     * @param df
     *            是否操作默认数据库
     * @param key
     * @param vals
     * @return
     */
    public boolean rpush(boolean df, String key, String... vals) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.rpush(key, vals);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 从一个list的最右边出队一个值
     * @param df
     * @param key
     * @return
     * @throws RedisConnException
     */
    public String rpop(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.rpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: rpop " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 从list中取出一个区间的值, 从左往右数为从0开始, 从右往左数为从-1开始
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @throws RedisConnException
     */
    public List<String> lrange(boolean df, String key, long start, long end)
            throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.lrange(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: lrange " + key + " "
                    + start + " " + end + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 取出list中的所有元素
     *
     * @param key
     * @return
     * @throws RedisConnException
     */
    public List<String> lAll(boolean df, String key) throws RedisConnException {
        return this.lrange(df, key, 0, -1);
    }


    public List<String> lAll(Long keyDB, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(keyDB);
            return j.lrange(key, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: lrange " + key + " "
                    + 0 + " " + -1 + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 放入一个k-v对到一个hash
     *
     * @param df 是否操作的默认数据库
     * @param key
     * @param field
     * @param val
     * @return
     */
    public boolean hset(boolean df, String key, String field, String val) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.hset(key, field, val);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean hset(Long keyDB, String key, String field, String val) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(keyDB);
            j.hset(key, field, val);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * get一个hash的所有值
     *
     * @param key
     * @return
     * @throws RedisConnException
     */
    public Map<String, String> hgetAll(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hgetAll(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hgetall " + key
                    + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 获取hash中一个字段的值
     *
     * @param key
     * @param field
     * @return
     * @throws RedisConnException
     */
    public String hget(boolean df, String key, String field) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hget(key, field);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hget " + key + " "
                    + field + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 获取hash中多个字段的值
     * @param key
     * @param fields
     * @return
     * @throws RedisConnException
     */
    public List<String> hmget(boolean df, String key, String...fields) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hmget(key, fields);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hget " + key + " "
                    + fields + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }


    public Set<String> hkeys(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hkeys(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hkeys " + key + " error, cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 删除一个hash中的多个field
     *
     * @param key
     * @param fields
     * @return
     * @throws RedisConnException
     */
    public boolean hdel(boolean df, String key, String... fields) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.hdel(key, fields);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public long hlen(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hlen(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hlen " + key + " error, cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean hexists(boolean df, String key, String field) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.hexists(key, field);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: hexists " + key + " " + field + " error, cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 在一个事务中执行多个命令
     * 该函数只适用于同一个db中的多个key, 0 <= db <= SmsContacts.REDIS_DEFAULT_DB
     * 在使用该方法之前请先确定操作的多个key属于同一个db
     * @param cmdPairs
     * @return
     */
    @SuppressWarnings("resource")
    public boolean oMulti(int db, List<RedisCmdPair> cmdPairs) {
        Jedis j = null;
        Transaction trans = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(new Long(db));
            trans = j.multi();

            for (RedisCmdPair cmdPair : cmdPairs) {
                String cmd = cmdPair.getCmd();

                Object[] oArgs = cmdPair.getoArgs();
                try {
                    MethodUtils.invokeMethod(trans, cmd, oArgs);
//					MethodUtils.invokeExactMethod(trans, cmd, oArgs);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            trans.exec();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (trans != null) {
                trans.discard();
            }
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 根据pattern查找匹配的key
     * 该函数用于查询一个db中的多个key, 0 <= db <= SmsContacts.REDIS_DEFAULT_DB
     * @param pattern
     * @return
     * @throws RedisConnException
     */
    public Set<String> keys(int db, String pattern) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(new Long(db));
            return j.keys(pattern);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: keys " + pattern + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }

    }

    /**
     * 重命名key, ok与nk不能相同
     * 该函数用于重命名一个db中的一个key, 0 <= db <= SmsContacts.REDIS_DEFAULT_DB
     * @param ok old key
     * @param nk new key
     * @return
     */
    public boolean rename(int db, String ok, String nk) {
        if (ok.equals(nk)) {
            return false;
        }
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(new Long(db));
            j.rename(ok, nk);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    /**
     * 向zset中插入一个或多个值
     * @param df 是否操作的是默认数据库
     * @param key zset的key
     * @param score val的分数, 用于排序
     * @param vals 值
     * @return
     */
    public boolean zadd(boolean df, String key, double score, String field) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.zadd(key, score, field);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public Double zscore(boolean df, String key, String field) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.zscore(key, field);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: zscore " + key + " " + field + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public Set<String> zrevrange(boolean df, String key, int start, int end) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.zrevrange(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: zrevrange " + key + " " + start + " " + end + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean zrem(boolean df, String key, String field) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.zrem(key, field);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public long llen(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.llen(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: llen " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public String ping() throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(defaultDb);
            return j.ping();
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: ping , cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public String spop(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.spop(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: spop " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public long incr(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.incr(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: incr " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public Long expire(boolean df, String key, int expire) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.expire(key, expire);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: incr " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean exist(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: exist " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean exist(int db, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(new Long(db));
            return j.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: exist " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean sadd(boolean df, String key, String field) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.sadd(key, field);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean srem(boolean df, String key, String field) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.srem(key, field);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public long scard(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.scard(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: scard " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public List<String> mget(int db, String...keys) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(new Long(db));
            return j.mget(keys);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: mget " + keys + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean sismember(boolean df, String key, String member) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.sismember(key, member);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: sismember " + key + " " + member + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean hincrby(boolean df, String key, String field, int increment) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.hincrBy(key, field, increment);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public long decr(boolean df, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            return j.decr(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: decr " + key + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean setex(boolean df, String key, int secs,
            String value) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.setex(key, secs, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: setex " + key + " " + secs + " " + value + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean hmset(boolean df, String key,  Map<String, String> hash) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(getKeyDB(df, key));
            j.hmset(key, hash);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public boolean del(boolean df, String... keys) {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = connect(defaultDb);
            j.del(keys);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            return false;
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

    public Set<String> smembers(boolean b, String key) throws RedisConnException {
        Jedis j = null;
        boolean borrowOrOprSuccess = true;
        try {
            j = this.connect(defaultDb);
            return j.smembers(key);
        } catch (Exception e) {
            e.printStackTrace();
            borrowOrOprSuccess = false;
            this.returnBrokenResource(j);
            throw new RedisConnException("redis command: smembers " + key
                    + ", cause: " + e.getMessage());
        } finally {
            if (borrowOrOprSuccess) {
                this.disConnected(j);
            }
        }
    }

}
