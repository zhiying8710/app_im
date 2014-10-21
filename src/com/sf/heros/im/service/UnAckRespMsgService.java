package com.sf.heros.im.service;

import com.sf.heros.im.common.bean.msg.RespMsg;

public interface UnAckRespMsgService {

    public String popFromQueue();

    public boolean pushToQueue(String unAckMsg);

    public boolean pushToQueue(RespMsg unAckMsg);

    public void add(String unAckMsgId);

    public void remove(String unAckMsgId);

    public void release();

}
