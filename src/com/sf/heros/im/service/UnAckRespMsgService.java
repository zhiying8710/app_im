package com.sf.heros.im.service;

import com.sf.heros.im.common.bean.msg.Resp;

public interface UnAckRespMsgService {

    public String popFromQueue();

    public boolean pushToQueue(String unAckMsg);

    public boolean pushToQueue(Resp unAckMsg);

    public void add(String msgNo);

    public void remove(String msgNo);

    public void release();

}
