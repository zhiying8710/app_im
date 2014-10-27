package com.sf.heros.im.service;

import com.sf.heros.im.common.bean.Session;

public interface SessionService {

    public boolean add(String id, Session session);

    public Session get(String id);

    public void updatePingTime(String id);

    public void del(String id);

    public Session kick(String id);

    public void delAll();

}
