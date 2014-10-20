package com.sf.heros.im.common;

import io.netty.channel.Channel;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Session {

    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_OFFLINE = 1;
    public static final int STATUS_KICKED = 2;

    private String id;
    private Channel channel;
    private Map<String, Object> data = new ConcurrentHashMap<String, Object>();
    private int status;
    private long pingTime;
    private long overtime = PropsLoader.get(Const.PropsConst.PING_OVERTIME, 10 * 1000);

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Session() {
    }

    public Session(String id, Channel channel,
            long pingTime, int status) {
        super();
        this.id = id;
        this.channel = channel;
        this.pingTime = pingTime;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public long getPingTime() {
        lock.readLock().lock();
        try {
            return pingTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPingTime(long pingTime) {
        lock.writeLock().lock();
        try {
            this.pingTime = pingTime;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean overtime() {
        return new Date().getTime() - getPingTime() > overtime;
    }

    public Object getAttr(String attrKey) {
        if (data == null) {
            return null;
        }
        return data.get(attrKey);
    }

    public void setAttr(String attrKey, Object val) {
        data.put(attrKey, val);
    }

}
