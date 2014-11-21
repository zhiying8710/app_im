package com.sf.heros.im.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.sf.heros.im.AppMain;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;

public interface Counter {

    public long incrAndGet();

    public long decrAndGet();

    public long get();

    public void init();

    static abstract class RedisAbstractCounter implements Counter {

        private static final RedisManagerV2 rm = RedisManagerV2.getInstance();
        private String key;
        private static String slot = AppMain.SERVER_ID;

        public RedisAbstractCounter(String key) {
            this.key = key;
        }

        @Override
        public long incrAndGet() {
            try {
                return rm.hincrby(key, slot, 1);
            } catch (RedisConnException e) {
                return -1;
            }
        }
        @Override
        public long decrAndGet() {
            try {
                return rm.hincrby(key, slot, -1);
            } catch (RedisConnException e) {
                return -1;
            }
        }
        @Override
        public long get() {
            long total = 0;
            try {
                Map<String, String> counts = rm.hgetAll(key);
                Set<Entry<String, String>> countEntries = counts.entrySet();
                for (Entry<String, String> countEntry : countEntries) {
                    String val = countEntry.getValue();
                    if (StringUtils.isNotBlank(val)) {
                        long count = Long.valueOf(val);
                        if (count > 0) {
                            total += count;
                        }
                    }
                }
                return total;
            } catch (RedisConnException e) {
                return -1;
            }
        }

        @Override
        public void init() {
            rm.hdel(key, slot);
        }

    }


    static class ConnsCounter extends RedisAbstractCounter {

        private static final String key = "__channels_count";

        public static final Counter INSTANCE = new ConnsCounter(key);

        private ConnsCounter(String key) {
            super(key);
        }

    }

    static class OnlinesCounter extends RedisAbstractCounter {

        private static final String key = "__onlines_count";

        public static final Counter INSTANCE = new OnlinesCounter(key);

        private OnlinesCounter(String key) {
            super(key);
        }

    }

//    private static final AtomicLong connsCounter = new AtomicLong();
//    private static final ReentrantReadWriteLock connsLock = new ReentrantReadWriteLock();
//    private static final AtomicLong onlinesCounter = new AtomicLong();
//    private static final ReentrantReadWriteLock onlinesLock = new ReentrantReadWriteLock();
//
//    public static long incrConnsAndGet() {
//        connsLock.writeLock().lock();
//        try {
//            return connsCounter.incrementAndGet();
//        } finally {
//            connsLock.writeLock().unlock();
//        }
//    }
//
//    public static long decrConnsAndGet() {
//        connsLock.writeLock().lock();
//        try {
//            return connsCounter.decrementAndGet();
//        } finally {
//            connsLock.writeLock().unlock();
//        }
//    }
//
//    public static long getConns() {
//        connsLock.readLock().lock();
//        try {
//            return connsCounter.get();
//        } finally {
//            connsLock.readLock().unlock();
//        }
//    }
//
//    public static long incrOnlinesAndGet() {
//        onlinesLock.writeLock().lock();
//        try {
//            return onlinesCounter.incrementAndGet();
//        } finally {
//            onlinesLock.writeLock().unlock();
//        }
//    }
//
//    public static long decrOnlinesAndGet() {
//        onlinesLock.writeLock().lock();
//        try {
//            return onlinesCounter.decrementAndGet();
//        } finally {
//            onlinesLock.writeLock().unlock();
//        }
//    }
//
//    public static long getOnlines() {
//        onlinesLock.readLock().lock();
//        try {
//            return onlinesCounter.get();
//        } finally {
//            onlinesLock.readLock().unlock();
//        }
//    }
//
//    public static void initOnlines() {
//        onlinesLock.readLock().lock();
//        try {
//            onlinesCounter.set(0);
//        } finally {
//            onlinesLock.readLock().unlock();
//        }
//    }
}
