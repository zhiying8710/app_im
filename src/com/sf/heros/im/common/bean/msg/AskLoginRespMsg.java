package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class AskLoginRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public AskLoginRespMsg() {
        setType(Const.RespMsgConst.TYPE_ASK_LOGIN);
    }

}
