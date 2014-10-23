package com.sf.heros.im.common;

import java.util.concurrent.atomic.AtomicLong;

public class Counter {

    private static final AtomicLong connsCounter = new AtomicLong();
    private static final AtomicLong onlinesCounter = new AtomicLong();

    public static long incrConnsAndGet() {
        return connsCounter.incrementAndGet();
    }

    public static long decrConnsAndGet() {
        return connsCounter.decrementAndGet();
    }

    public static long getConns() {
        return connsCounter.get();
    }

    public static long incrOnlinesAndGet() {
        return onlinesCounter.incrementAndGet();
    }

    public static long decrsOnlinesAndGet() {
        return onlinesCounter.decrementAndGet();
    }

    public static long getOnlines() {
        return onlinesCounter.get();
    }

    public static void initOnlines() {
        onlinesCounter.set(0);
    }
}
