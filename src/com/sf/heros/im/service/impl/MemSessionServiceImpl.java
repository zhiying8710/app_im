package com.sf.heros.im.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.service.SessionService;

public class MemSessionServiceImpl implements SessionService {

    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public boolean add(String id, Session session) {
        sessions.put(id, session);
        return true;
    }

    @Override
    public Session get(String id) {
        return sessions.get(id);
    }

    @Override
    public void updatePingTime(String id) {
        Session session = sessions.get(id);
        if (session != null) {
            session.setPingTime(new Date().getTime());
        }
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
