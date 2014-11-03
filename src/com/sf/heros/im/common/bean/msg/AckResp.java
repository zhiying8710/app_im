package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class AckResp extends Resp {

    private static final long serialVersionUID = 1L;

    public AckResp(Long sid, String srcMsgNo, String srcFrom, String srcTo) {
        super(sid, false);
        setType(Const.RespConst.TYPE_ACK);
        setSrcMsgNo(srcMsgNo);
        setSrcFrom(srcFrom);
        setSrcTo(srcTo);
        setTime();
    }

    private void setSrcMsgNo(String srcMsgNo) {
        setToData(Const.RespAckConst.DATA_SRC_MSG_NO, srcMsgNo);
    }

    private void setSrcFrom(String srcFrom) {
        setToData(Const.RespAckConst.DATA_SRC_FROM_USERID, srcFrom);
    }

    private void setSrcTo(String srcTo) {
        setToData(Const.RespAckConst.DATA_SRC_TO_USERID, srcTo);
    }
}
