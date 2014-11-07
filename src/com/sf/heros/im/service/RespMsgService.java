package com.sf.heros.im.service;

import java.util.List;

import com.sf.heros.im.common.bean.msg.Resp;

public interface RespMsgService {

    public String getUnAck(String unAckRespMsgId);

    public Resp getUnAckMsg(String unAckRespMsgId);

    public void saveUnAck(String unAckRespMsgId, Resp respMsg);

    public void saveOffline(String userId, Resp respMsg);

    public List<String> getOfflines(String userId);

    public List<Resp> getOfflineMsgs(String userId);

    public void delUnAck(String unAckMsgIdFromAck);

    public void delOffline(String userId, String msgNo);
}
