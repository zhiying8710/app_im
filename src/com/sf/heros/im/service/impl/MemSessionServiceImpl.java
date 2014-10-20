package com.sf.heros.im.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sf.heros.im.common.Session;
import com.sf.heros.im.service.SessionService;

public class MemSessionServiceImpl implements SessionService {

    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public void add(String id, Session session) {
        sessions.put(id, session);
    }

    @Override
    public Session get(String id) {
        return sessions.get(id);
    }

    @Override
    public Collection<Session> getAll() {
        return sessions.values();
    }

    @Override
    public long size() {
        return sessions.size();
    }

    @Override
    public void updatePingTime(String id) {
        sessions.get(id).setPingTime(new Date().getTime());
    }

    @Override
    public void del(String id) {
        sessions.remove(id);
    }

    @Override
    public Session kick(String id) {
        if (id == null) {
            return null;
        }
        Session session = sessions.get(id);
        if (session == null) {
            return null;
        }
        session.setStatus(Session.STATUS_KICKED);
        return session;
    }

    @Override
    public void delAll() {
        sessions.clear();
        sessions = new ConcurrentHashMap<String, Session>();
    }

}
