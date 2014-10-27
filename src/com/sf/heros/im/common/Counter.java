package com.sf.heros.im.common;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {

    private static final AtomicLong connsCounter = new AtomicLong();
    private static final ReentrantReadWriteLock connsLock = new ReentrantReadWriteLock();
    private static final AtomicLong onlinesCounter = new AtomicLong();
    private static final ReentrantReadWriteLock onlinesLock = new ReentrantReadWriteLock();

    public static long incrConnsAndGet() {
        connsLock.writeLock().lock();
        try {
            return connsCounter.incrementAndGet();
        } finally {
            connsLock.writeLock().unlock();
        }
    }

    public static long decrConnsAndGet() {
        connsLock.writeLock().lock();
        try {
            return connsCounter.decrementAndGet();
        } finally {
            connsLock.writeLock().unlock();
        }
    }

    public static long getConns() {
        connsLock.readLock().lock();
        try {
            return connsCounter.get();
        } finally {
            connsLock.readLock().unlock();
        }
    }

    public static long incrOnlinesAndGet() {
        onlinesLock.writeLock().lock();
        try {
            return onlinesCounter.incrementAndGet();
        } finally {
            onlinesLock.writeLock().unlock();
        }
    }

    public static long decrOnlinesAndGet() {
        onlinesLock.writeLock().lock();
        try {
            return onlinesCounter.decrementAndGet();
        } finally {
            onlinesLock.writeLock().unlock();
        }
    }

    public static long getOnlines() {
        onlinesLock.readLock().lock();
        try {
            return onlinesCounter.get();
        } finally {
            onlinesLock.readLock().unlock();
        }
    }

    public static void initOnlines() {
        onlinesLock.readLock().lock();
        try {
            onlinesCounter.set(0);
        } finally {
            onlinesLock.readLock().unlock();
        }
    }
}
