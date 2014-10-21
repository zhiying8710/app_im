package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class AckRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public AckRespMsg(String srcTime, String srcFrom, String srcTo, int srcType) {
        setType(Const.RespMsgConst.TYPE_ACK);
        setSrcTime(srcTime);
        setSrcFrom(srcFrom);
        setSrcTo(srcTo);
        setSrcType(srcType);
    }

    private void setSrcTime(String srcTime) {
        setToData(Const.RespAckMsgConst.DATA_SRC_TIME, srcTime);
    }

    private void setSrcFrom(String srcFrom) {
        setToData(Const.RespAckMsgConst.DATA_SRC_FROM_USERID, srcFrom);
    }

    private void setSrcTo(String srcTo) {
        setToData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, srcTo);
    }

    private void setSrcType(int srcType) {
        setToData(Const.RespAckMsgConst.DATA_SRC_TYPE, srcType + "");
    }
}
