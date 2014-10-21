package com.sf.heros.im.service;

import java.util.List;

import com.sf.heros.im.common.bean.msg.RespMsg;

public interface RespMsgService {

    public String getUnAck(String unAckRespMsgId);

    public RespMsg getUnAckMsg(String unAckRespMsgId);

    public void saveUnAck(String unAckRespMsgId, RespMsg respMsg);

    public void saveOffline(String userId, RespMsg respMsg);

    public List<String> getOfflines(String userId);

    public List<RespMsg> getOfflineMsgs(String userId);

    public void delUnAck(String unAckMsgIdFromAck);

}
