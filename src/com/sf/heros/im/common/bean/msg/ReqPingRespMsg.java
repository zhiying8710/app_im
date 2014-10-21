package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class ReqPingRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public ReqPingRespMsg() {
        setType(Const.RespMsgConst.TYPE_REQ_PING);
    }

}
