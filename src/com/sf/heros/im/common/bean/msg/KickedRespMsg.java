package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class KickedRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public KickedRespMsg() {
        setType(Const.RespMsgConst.TYPE_KICKED);
    }

}
