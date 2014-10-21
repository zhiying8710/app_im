package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class ServErrRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public ServErrRespMsg() {
        setType(Const.RespMsgConst.TYPE_SERVER_ERR);
    }

}
