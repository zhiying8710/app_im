package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class ErrAckResp extends Resp {

    private static final long serialVersionUID = 1L;

    public ErrAckResp(Long sid, int type, String srcMsgNo) {
        super(sid, false);
        setType(type);
        setSrcMsgNo(srcMsgNo);
    }

    private void setSrcMsgNo(String srcMsgNo) {
        setToData(Const.RespAckConst.DATA_SRC_MSG_NO, srcMsgNo);
    }

}
