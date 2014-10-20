package com.sf.heros.im.service;

import java.util.Collection;

import com.sf.heros.im.common.Session;

public interface SessionService {

    public void add(String id, Session session);

    public Session get(String id);

    public Collection<Session> getAll();

    public long size();

    public void updatePingTime(String id);

    public void del(String id);

    public Session kick(String id);

    public void delAll();

}
