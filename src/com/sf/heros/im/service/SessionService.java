package com.sf.heros.im.service;

import com.sf.heros.im.common.bean.Session;

public interface SessionService {

    public boolean add(Long id, Session session);

    public Session get(Long id);

    public void updatePingTime(Long id);

    public void del(Long id);

    public Session kick(Long id);

    public void delAll();

}
