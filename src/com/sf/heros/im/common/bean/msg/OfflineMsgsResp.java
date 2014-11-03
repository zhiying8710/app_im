package com.sf.heros.im.common.bean.msg;

import java.util.List;

import com.sf.heros.im.common.Const;

public class OfflineMsgsResp extends Resp {

    private static final long serialVersionUID = 1L;

    public OfflineMsgsResp(Long sid, List<String> offlineMsgs, String from, String to) {
        super(sid, false);
        setType(Const.RespConst.TYPE_OFFLINE_MSG);
        setOfflineMsgs(offlineMsgs);
        setFrom(from);
        setTo(to);
    }

    private void setOfflineMsgs(List<String> offlineMsgs) {
        setToData(Const.RespConst.DATA_KEY_OFFLINE_MSGS, offlineMsgs);
    }

    private void setFrom(String from) {
        setToData(Const.RespConst.DATA_KEY_FROM_USER_ID, from);
    }

    private void setTo(String to) {
        setToData(Const.RespConst.DATA_KEY_TO_USER_ID, to);
    }

}
